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
import com.ijackey.iMusic.data.api.OnlineTrack
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
    private val musicSearchApi: MusicSearchApi
) {
    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()
    
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
        val audioExtensions = listOf("mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus")
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
}