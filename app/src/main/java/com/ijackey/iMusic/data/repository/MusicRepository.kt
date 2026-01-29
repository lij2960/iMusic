package com.ijackey.iMusic.data.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import com.ijackey.iMusic.data.database.SongDao
import com.ijackey.iMusic.data.model.Song
import com.ijackey.iMusic.data.api.LyricsApi
import com.ijackey.iMusic.data.api.MusicSearchApi
import com.ijackey.iMusic.data.api.FangpiApi
import com.ijackey.iMusic.data.api.FangpiTrack
import com.ijackey.iMusic.data.api.OnlineTrack
import com.ijackey.iMusic.data.parser.FangpiParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val songDao: SongDao,
    private val context: Context,
    private val lyricsApi: LyricsApi,
    private val musicSearchApi: MusicSearchApi,
    private val fangpiApi: FangpiApi
) {
    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()
    
    suspend fun getAllSongsSync(): List<Song> = songDao.getAllSongsSync()
    
    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)
    
    suspend fun scanMusicFromDirectory(directoryPath: String) = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val directory = File(directoryPath)
        
        if (directory.exists() && directory.isDirectory) {
            directory.walkTopDown().forEach { file ->
                if (file.isFile && isAudioFile(file)) {
                    val song = createSongFromFile(file)
                    song?.let { songs.add(it) }
                }
            }
        }
        
        if (songs.isNotEmpty()) {
            songDao.insertSongs(songs)
        }
    }
    
    suspend fun scanAllMusic() = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.ALBUM_ID
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1"
        
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC"
        )
        
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            
            while (it.moveToNext()) {
                val id = it.getLong(idColumn).toString()
                val title = it.getString(titleColumn) ?: "Unknown"
                val artist = it.getString(artistColumn) ?: "Unknown Artist"
                val album = it.getString(albumColumn) ?: "Unknown Album"
                val duration = it.getLong(durationColumn)
                val path = it.getString(dataColumn) ?: ""
                val dateAdded = it.getLong(dateAddedColumn) * 1000 // Convert to milliseconds
                val size = it.getLong(sizeColumn)
                val albumId = it.getLong(albumIdColumn)
                
                // Get album art URI and path
                val albumArtUri = try {
                    ContentUris.withAppendedId(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        albumId
                    ).toString()
                } catch (e: Exception) {
                    null
                }
                
                val albumArtPath = getAlbumArtPath(path)
                
                if (path.isNotEmpty() && File(path).exists()) {
                    songs.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            path = path,
                            dateAdded = dateAdded,
                            size = size,
                            albumArt = albumArtUri,
                            albumArtPath = albumArtPath
                        )
                    )
                }
            }
        }
        
        if (songs.isNotEmpty()) {
            // Clear existing songs and insert new ones
            songDao.deleteAllSongs()
            songDao.insertSongs(songs)
        }
    }
    
    private fun isAudioFile(file: File): Boolean {
        val audioExtensions = listOf(
            "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus",
            "mp4", "3gp", "amr", "awb", "wv", "ape", "dts", "ac3"
        )
        return audioExtensions.any { file.extension.lowercase() == it }
    }
    
    private fun createSongFromFile(file: File): Song? {
        return try {
            val retriever = MediaMetadataRetriever()
            var title = file.nameWithoutExtension
            var artist = "Unknown Artist"
            var album = "Unknown Album"
            var duration = 0L
            
            try {
                retriever.setDataSource(file.absolutePath)
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: title
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: artist
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: album
                duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            } catch (e: Exception) {
                // Use file name as fallback
            } finally {
                try {
                    retriever.release()
                } catch (e: Exception) {
                    // Ignore
                }
            }
            
            Song(
                id = file.absolutePath.hashCode().toString(),
                title = title,
                artist = artist,
                album = album,
                duration = duration,
                path = file.absolutePath,
                dateAdded = file.lastModified(),
                size = file.length()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    fun getLyricsForSong(song: Song): String? {
        return try {
            val musicFile = File(song.path)
            val fileName = musicFile.nameWithoutExtension
            
            // First try app's internal lyrics directory
            val internalLyricsDir = File(context.filesDir, "lyrics")
            val internalLyricsFile = File(internalLyricsDir, "$fileName.lrc")
            
            if (internalLyricsFile.exists()) {
                println("Found internal lyrics: ${internalLyricsFile.absolutePath}")
                return internalLyricsFile.readText(Charsets.UTF_8)
            }
            
            // Then try original locations (for existing lyrics files)
            val basePath = song.path.substringBeforeLast(".")
            val directory = File(song.path).parent ?: return null
            
            val possibleLyricFiles = listOf(
                File("$basePath.lrc"),
                File("$basePath.txt"),
                File("$directory/$fileName.lrc"),
                File("$directory/$fileName.txt")
            )
            
            for (lyricFile in possibleLyricFiles) {
                if (lyricFile.exists() && lyricFile.canRead()) {
                    println("Found external lyrics: ${lyricFile.absolutePath}")
                    return lyricFile.readText(Charsets.UTF_8)
                }
            }
            
            null
        } catch (e: Exception) {
            println("Error reading lyrics: ${e.message}")
            null
        }
    }
    
    // Online lyrics search
    suspend fun searchOnlineLyrics(title: String, artist: String): String? {
        return try {
            val keywords = "$title $artist"
            Log.d("MusicRepository", "API call: searching music for lyrics: $keywords")
            
            val searchResponse = musicSearchApi.searchMusic(keywords)
            Log.d("MusicRepository", "Search API response code: ${searchResponse.code()}")
            
            if (searchResponse.isSuccessful) {
                val songs = searchResponse.body()?.result?.songs
                Log.d("MusicRepository", "Found ${songs?.size ?: 0} songs")
                if (!songs.isNullOrEmpty()) {
                    val firstSong = songs[0]
                    val songId = firstSong.id
                    Log.d("MusicRepository", "Found song ID: $songId, getting lyrics...")
                    
                    val lyricsResponse = lyricsApi.getLyrics(songId)
                    Log.d("MusicRepository", "Lyrics API response code: ${lyricsResponse.code()}")
                    
                    if (lyricsResponse.isSuccessful) {
                        val lyrics = lyricsResponse.body()?.lrc?.lyric
                        Log.d("MusicRepository", "Lyrics found: ${lyrics?.take(100)}...")
                        return lyrics
                    } else {
                        Log.e("MusicRepository", "Lyrics API error: ${lyricsResponse.errorBody()?.string()}")
                        return null
                    }
                } else {
                    Log.d("MusicRepository", "No songs found in search")
                    return null
                }
            } else {
                Log.e("MusicRepository", "Search API error: ${searchResponse.errorBody()?.string()}")
                return null
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Exception in searchOnlineLyrics: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
    
    suspend fun saveLyricsForSong(song: Song, lyrics: String) {
        try {
            // Use app's private directory instead of external storage
            val musicFile = File(song.path)
            val fileName = musicFile.nameWithoutExtension
            
            // Save to app's internal files directory
            val lyricsDir = File(context.filesDir, "lyrics")
            if (!lyricsDir.exists()) {
                lyricsDir.mkdirs()
            }
            
            val lyricsFile = File(lyricsDir, "$fileName.lrc")
            lyricsFile.writeText(lyrics, Charsets.UTF_8)
            println("Lyrics saved to: ${lyricsFile.absolutePath}")
        } catch (e: Exception) {
            println("Error saving lyrics: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Online music search
    suspend fun searchOnlineMusic(query: String): List<OnlineTrack> {
        return try {
            Log.d("MusicRepository", "API call: searching music for $query")
            val response = musicSearchApi.searchMusic(query)
            Log.d("MusicRepository", "Music API response code: ${response.code()}")
            if (response.isSuccessful) {
                val tracks = response.body()?.result?.songs ?: emptyList()
                Log.d("MusicRepository", "Found ${tracks.size} tracks")
                // Print album art URLs for debugging
                tracks.forEach { track ->
                    Log.d("ALBUM_ART", "Track: ${track.name} by ${track.artists.firstOrNull()?.name}")
                    Log.d("ALBUM_ART", "Album art URL: ${track.album.artist.img1v1Url}")
                }
                tracks
            } else {
                Log.e("MusicRepository", "Music API error: ${response.errorBody()?.string()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Exception in searchOnlineMusic: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    // Download album art
    suspend fun downloadAlbumArt(song: Song, imageUrl: String): String? {
        return try {
            Log.d("ALBUM_ART", "Starting download from: $imageUrl")
            
            val musicFile = File(song.path)
            val fileName = musicFile.nameWithoutExtension
            
            val artDir = File(context.filesDir, "album_art")
            if (!artDir.exists()) {
                artDir.mkdirs()
                Log.d("ALBUM_ART", "Created directory: ${artDir.absolutePath}")
            }
            
            val artFile = File(artDir, "$fileName.jpg")
            Log.d("ALBUM_ART", "Target file: ${artFile.absolutePath}")
            
            withContext(Dispatchers.IO) {
                val connection = URL(imageUrl).openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
                
                connection.getInputStream().use { input ->
                    artFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytes = 0
                        
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytes += bytesRead
                        }
                        
                        Log.d("ALBUM_ART", "Downloaded $totalBytes bytes")
                    }
                }
            }
            
            if (artFile.exists() && artFile.length() > 0) {
                Log.d("ALBUM_ART", "Saved successfully: ${artFile.absolutePath} (${artFile.length()} bytes)")
                // Update song in database with new album art path
                val updatedSong = song.copy(albumArtPath = artFile.absolutePath)
                songDao.updateSong(updatedSong)
                Log.d("ALBUM_ART", "Updated song in database")
                artFile.absolutePath
            } else {
                Log.e("ALBUM_ART", "Failed to save - file empty or doesn't exist")
                null
            }
        } catch (e: Exception) {
            Log.e("ALBUM_ART", "Error downloading: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    private fun getAlbumArtPath(musicPath: String): String? {
        return try {
            val musicFile = File(musicPath)
            val fileName = musicFile.nameWithoutExtension
            
            // First check app's internal album art directory
            val internalArtDir = File(context.filesDir, "album_art")
            val internalArtFile = File(internalArtDir, "$fileName.jpg")
            
            if (internalArtFile.exists()) {
                return internalArtFile.absolutePath
            }
            
            // Then check original locations
            val directory = musicFile.parentFile ?: return null
            val artFiles = listOf(
                "$fileName.jpg", "$fileName.jpeg", "$fileName.png",
                "cover.jpg", "cover.jpeg", "cover.png",
                "folder.jpg", "folder.jpeg", "folder.png",
                "album.jpg", "album.jpeg", "album.png"
            )
            
            for (artFile in artFiles) {
                val file = File(directory, artFile)
                if (file.exists()) {
                    return file.absolutePath
                }
            }
            
            null
        } catch (e: Exception) {
            null
        }
    }
    
    // Search album art only
    suspend fun searchOnlineAlbumArt(song: Song): String? {
        return try {
            val keywords = "${song.title} ${song.artist}"
            Log.d("MusicRepository", "Searching album art only for: $keywords")
            
            val searchResponse = musicSearchApi.searchMusic(keywords)
            Log.d("MusicRepository", "Search response code: ${searchResponse.code()}")
            
            if (searchResponse.isSuccessful) {
                val songs = searchResponse.body()?.result?.songs
                if (!songs.isNullOrEmpty()) {
                    val firstSong = songs[0]
                    val albumArtUrl = firstSong.album.artist.img1v1Url
                    
                    Log.d("ALBUM_ART", "Found album art URL: $albumArtUrl")
                    
                    // Download album art if URL is available
                    if (!albumArtUrl.isNullOrEmpty()) {
                        downloadAlbumArt(song, albumArtUrl)
                    } else {
                        Log.d("ALBUM_ART", "No album art URL found")
                        null
                    }
                } else {
                    Log.d("MusicRepository", "No songs found")
                    null
                }
            } else {
                Log.e("MusicRepository", "Search error: ${searchResponse.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Exception: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    // Search multiple lyrics options
    suspend fun searchMultipleLyrics(title: String, artist: String): List<Pair<String, String>> {
        return try {
            val keywords = "$title $artist"
            Log.d("MusicRepository", "Searching multiple lyrics for: $keywords")
            
            val searchResponse = musicSearchApi.searchMusic(keywords, limit = 10)
            if (searchResponse.isSuccessful) {
                val songs = searchResponse.body()?.result?.songs ?: emptyList()
                val lyricsOptions = mutableListOf<Pair<String, String>>()
                
                for (song in songs.take(5)) {
                    try {
                        val lyricsResponse = lyricsApi.getLyrics(song.id)
                        if (lyricsResponse.isSuccessful) {
                            val lyrics = lyricsResponse.body()?.lrc?.lyric
                            if (!lyrics.isNullOrBlank()) {
                                val displayName = "${song.name} - ${song.artists.firstOrNull()?.name ?: "Unknown"}"
                                lyricsOptions.add(Pair(displayName, lyrics))
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MusicRepository", "Error getting lyrics for song ${song.id}: ${e.message}")
                    }
                }
                lyricsOptions
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Exception in searchMultipleLyrics: ${e.message}")
            emptyList()
        }
    }
    
    // Fangpi music search and download
    suspend fun searchFangpiMusic(keyword: String): List<FangpiTrack> {
        return try {
            Log.d("MusicRepository", "Searching Fangpi music for: $keyword")
            val encodedKeyword = FangpiParser.encodeKeyword(keyword)
            val response = fangpiApi.searchMusic(encodedKeyword)
            
            if (response.isSuccessful) {
                val html = response.body() ?: ""
                val tracks = FangpiParser.parseSearchResults(html)
                Log.d("MusicRepository", "Found ${tracks.size} Fangpi tracks")
                tracks
            } else {
                Log.e("MusicRepository", "Fangpi search error: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Exception in searchFangpiMusic: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun downloadFangpiMusic(track: FangpiTrack): Boolean {
        return try {
            Log.d("MusicRepository", "Getting download URL for: ${track.title}")
            
            // First get the music detail page to find download URL
            val detailResponse = fangpiApi.getMusicDetail(track.id)
            if (!detailResponse.isSuccessful) {
                Log.e("MusicRepository", "Failed to get music detail")
                return false
            }
            
            val detailHtml = detailResponse.body() ?: ""
            val downloadUrl = FangpiParser.parseDownloadUrl(detailHtml)
            
            if (downloadUrl == null) {
                Log.e("MusicRepository", "No download URL found")
                return false
            }
            
            Log.d("MusicRepository", "Found download URL: $downloadUrl")
            
            // 如果是夸克网盘链接，尝试跳转到夸克浏览器
            if (downloadUrl.contains("quark.cn") || downloadUrl.contains("pan.")) {
                // 清理转义字符
                val cleanUrl = downloadUrl.replace("\\/", "/")
                Log.i("MusicRepository", "Quark pan link detected: $cleanUrl")
                Log.i("MusicRepository", "Attempting to open in Quark browser...")
                
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(cleanUrl))
                    intent.setPackage("com.quark.browser") // 夸克浏览器包名
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    Log.i("MusicRepository", "Successfully opened Quark browser")
                    return true // 成功跳转到夸克浏览器
                } catch (e: Exception) {
                    Log.w("MusicRepository", "Quark browser not found, trying default browser")
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(cleanUrl))
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        Log.i("MusicRepository", "Opened in default browser")
                        return true // 成功跳转到默认浏览器
                    } catch (e2: Exception) {
                        Log.e("MusicRepository", "Failed to open browser: ${e2.message}")
                        return false
                    }
                }
            }
            
            // 其他直接下载链接的处理逻辑保持不变
            val downloadResponse = fangpiApi.downloadMusic(downloadUrl)
            if (!downloadResponse.isSuccessful) {
                Log.e("MusicRepository", "Failed to download music")
                return false
            }
            
            val responseBody = downloadResponse.body()
            if (responseBody == null) {
                Log.e("MusicRepository", "Empty response body")
                return false
            }
            
            // Save to downloads directory
            val downloadsDir = File(context.getExternalFilesDir(null), "downloads")
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val fileName = "${track.title} - ${track.artist}.mp3"
                .replace("[^a-zA-Z0-9\\u4e00-\\u9fa5\\s\\-_]".toRegex(), "")
            val musicFile = File(downloadsDir, fileName)
            
            withContext(Dispatchers.IO) {
                responseBody.byteStream().use { input ->
                    musicFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            
            if (musicFile.exists() && musicFile.length() > 0) {
                Log.d("MusicRepository", "Downloaded: ${musicFile.absolutePath}")
                
                // Add to database
                val song = Song(
                    id = musicFile.absolutePath.hashCode().toString(),
                    title = track.title,
                    artist = track.artist,
                    album = "Downloaded",
                    duration = 0L,
                    path = musicFile.absolutePath,
                    dateAdded = System.currentTimeMillis(),
                    size = musicFile.length()
                )
                songDao.insertSongs(listOf(song))
                
                true
            } else {
                Log.e("MusicRepository", "Download failed - file empty")
                false
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Exception in downloadFangpiMusic: ${e.message}")
            false
        }
    }
    
    // Search multiple album art options
    suspend fun searchMultipleAlbumArt(title: String, artist: String): List<Pair<String, String>> {
        return try {
            val keywords = "$title $artist"
            Log.d("MusicRepository", "Searching multiple album art for: $keywords")
            
            val searchResponse = musicSearchApi.searchMusic(keywords, limit = 10)
            if (searchResponse.isSuccessful) {
                val songs = searchResponse.body()?.result?.songs ?: emptyList()
                val artOptions = mutableListOf<Pair<String, String>>()
                
                for (song in songs.take(5)) {
                    val albumArtUrl = song.album.artist.img1v1Url
                    if (!albumArtUrl.isNullOrBlank()) {
                        val displayName = "${song.name} - ${song.artists.firstOrNull()?.name ?: "Unknown"}"
                        artOptions.add(Pair(displayName, albumArtUrl))
                    }
                }
                artOptions
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Exception in searchMultipleAlbumArt: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun deleteSong(song: Song): Boolean {
        return try {
            // Delete from database
            songDao.deleteSong(song)
            
            // Delete physical file
            val file = File(song.path)
            if (file.exists()) {
                file.delete()
            }
            
            // Delete associated files (lyrics, album art)
            val musicFile = File(song.path)
            val fileName = musicFile.nameWithoutExtension
            
            // Delete lyrics
            val lyricsDir = File(context.filesDir, "lyrics")
            val lyricsFile = File(lyricsDir, "$fileName.lrc")
            if (lyricsFile.exists()) {
                lyricsFile.delete()
            }
            
            // Delete album art
            val artDir = File(context.filesDir, "album_art")
            val artFile = File(artDir, "$fileName.jpg")
            if (artFile.exists()) {
                artFile.delete()
            }
            
            true
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error deleting song: ${e.message}")
            false
        }
    }
}