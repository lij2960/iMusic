package com.ijackey.iMusic.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.ijackey.iMusic.data.model.PlayMode
import com.ijackey.iMusic.data.model.Song
import com.ijackey.iMusic.data.model.SortOrder
import com.ijackey.iMusic.data.model.EqualizerPreset
import com.ijackey.iMusic.data.api.OnlineTrack
import com.ijackey.iMusic.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val exoPlayer: ExoPlayer,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("music_prefs", Context.MODE_PRIVATE)
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _playMode = MutableStateFlow(PlayMode.SEQUENTIAL)
    val playMode: StateFlow<PlayMode> = _playMode.asStateFlow()
    
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_ADDED)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist.asStateFlow()
    
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()
    
    // Equalizer states
    private val _equalizerBands = MutableStateFlow(listOf(0f, 0f, 0f, 0f, 0f))
    val equalizerBands: StateFlow<List<Float>> = _equalizerBands.asStateFlow()
    
    private val _currentEqualizerPreset = MutableStateFlow("正常")
    val currentEqualizerPreset: StateFlow<String> = _currentEqualizerPreset.asStateFlow()
    
    // Online search states - removed
    // private val _onlineSearchResults = MutableStateFlow<List<OnlineTrack>>(emptyList())
    // val onlineSearchResults: StateFlow<List<OnlineTrack>> = _onlineSearchResults.asStateFlow()
    
    // private val _isSearching = MutableStateFlow(false)
    // val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    val songs: StateFlow<List<Song>> = combine(
        musicRepository.getAllSongs(),
        sortOrder,
        searchQuery
    ) { allSongs, order, query ->
        val filteredSongs = if (query.isBlank()) {
            allSongs
        } else {
            allSongs.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.artist.contains(query, ignoreCase = true) ||
                it.path.substringAfterLast("/").contains(query, ignoreCase = true)
            }
        }
        
        when (order) {
            SortOrder.DATE_ADDED -> filteredSongs.sortedByDescending { it.dateAdded }
            SortOrder.TITLE -> filteredSongs.sortedBy { it.title }
            SortOrder.ARTIST -> filteredSongs.sortedBy { it.artist }
            SortOrder.DURATION -> filteredSongs.sortedByDescending { it.duration }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        setupPlayer()
        loadLastPlayedSong()
        loadEqualizerSettings()
        
        viewModelScope.launch {
            songs.collect { songList ->
                _playlist.value = songList
            }
        }
    }
    
    private fun setupPlayer() {
        // Configure audio attributes for high quality playback
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        
        exoPlayer.setAudioAttributes(audioAttributes, true)
        
        // Configure track selector for best audio quality
        val trackSelector = exoPlayer.trackSelector as? DefaultTrackSelector
        trackSelector?.let { selector ->
            selector.parameters = selector.buildUponParameters()
                .setMaxAudioBitrate(Int.MAX_VALUE)
                .setForceHighestSupportedBitrate(true)
                .build()
        }
        
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let { item ->
                    val song = _playlist.value.find { it.path == item.localConfiguration?.uri.toString() }
                    _currentSong.value = song
                    song?.let { 
                        saveLastPlayedSong(it)
                        updateCurrentIndex(it)
                    }
                }
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> handleSongEnd()
                    Player.STATE_IDLE -> {
                        // Player is idle, might be due to error
                        android.util.Log.d("Player", "Player is idle")
                    }
                }
            }
            
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("Player", "Playback error: ${error.message}")
                // Skip to next song on error
                skipToNext()
            }
        })
        
        // Update position periodically
        viewModelScope.launch {
            while (true) {
                if (exoPlayer.isPlaying) {
                    _currentPosition.value = exoPlayer.currentPosition
                    _duration.value = exoPlayer.duration.takeIf { it > 0 } ?: 0L
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }
    
    private fun handleSongEnd() {
        when (_playMode.value) {
            PlayMode.SEQUENTIAL -> skipToNext()
            PlayMode.SHUFFLE -> skipToNext()
            PlayMode.REPEAT_ONE -> {
                // Repeat current song
                exoPlayer.seekTo(0)
                exoPlayer.play()
            }
        }
    }
    
    private fun updateCurrentIndex(song: Song) {
        val index = _playlist.value.indexOf(song)
        if (index != -1) {
            _currentIndex.value = index
        }
    }
    
    fun playSong(song: Song) {
        val index = _playlist.value.indexOf(song)
        if (index != -1) {
            _currentIndex.value = index
            _currentSong.value = song
            
            val mediaItem = MediaItem.fromUri(song.path)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            
            saveLastPlayedSong(song)
        }
    }
    
    fun playPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (_currentSong.value == null && _playlist.value.isNotEmpty()) {
                playSong(_playlist.value[0])
            } else {
                exoPlayer.play()
            }
        }
    }
    
    fun skipToNext() {
        val currentIdx = _currentIndex.value
        val playlist = _playlist.value
        
        if (playlist.isNotEmpty()) {
            val nextIndex = when (_playMode.value) {
                PlayMode.SEQUENTIAL -> (currentIdx + 1) % playlist.size
                PlayMode.SHUFFLE -> {
                    var randomIndex: Int
                    do {
                        randomIndex = (0 until playlist.size).random()
                    } while (randomIndex == currentIdx && playlist.size > 1)
                    randomIndex
                }
                PlayMode.REPEAT_ONE -> (currentIdx + 1) % playlist.size
            }
            
            if (nextIndex < playlist.size) {
                playSong(playlist[nextIndex])
            }
        }
    }
    
    fun skipToPrevious() {
        val currentIdx = _currentIndex.value
        val playlist = _playlist.value
        
        if (playlist.isNotEmpty()) {
            val prevIndex = when (_playMode.value) {
                PlayMode.SEQUENTIAL -> if (currentIdx > 0) currentIdx - 1 else playlist.size - 1
                PlayMode.SHUFFLE -> {
                    var randomIndex: Int
                    do {
                        randomIndex = (0 until playlist.size).random()
                    } while (randomIndex == currentIdx && playlist.size > 1)
                    randomIndex
                }
                PlayMode.REPEAT_ONE -> if (currentIdx > 0) currentIdx - 1 else playlist.size - 1
            }
            
            if (prevIndex >= 0 && prevIndex < playlist.size) {
                playSong(playlist[prevIndex])
            }
        }
    }
    
    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }
    
    fun setPlayMode(mode: PlayMode) {
        _playMode.value = mode
        prefs.edit().putString("play_mode", mode.name).apply()
    }
    
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        prefs.edit().putString("sort_order", order.name).apply()
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun scanMusic() {
        viewModelScope.launch {
            musicRepository.scanAllMusic()
        }
    }
    
    fun scanMusicFromDirectory(path: String) {
        viewModelScope.launch {
            musicRepository.scanMusicFromDirectory(path)
        }
    }
    
    fun getLyrics(song: Song): String? {
        return musicRepository.getLyricsForSong(song)
    }
    
    // Online lyrics search only (no album art)
    fun searchOnlineLyrics(song: Song) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ViewModel", "Searching lyrics only for: ${song.title} by ${song.artist}")
                val lyrics = musicRepository.searchOnlineLyrics(song.title, song.artist)
                
                if (lyrics != null) {
                    android.util.Log.d("ViewModel", "Found lyrics, saving...")
                    musicRepository.saveLyricsForSong(song, lyrics)
                } else {
                    android.util.Log.d("ViewModel", "No lyrics found")
                }
            } catch (e: Exception) {
                android.util.Log.e("ViewModel", "Error searching lyrics: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    // Online album art search only
    fun searchOnlineAlbumArt(song: Song) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ViewModel", "Searching album art only for: ${song.title} by ${song.artist}")
                val artPath = musicRepository.searchOnlineAlbumArt(song)
                
                if (artPath != null) {
                    android.util.Log.d("ALBUM_ART", "Album art downloaded, updating UI")
                    // Update current song if it matches
                    val currentSongValue = _currentSong.value
                    if (currentSongValue?.id == song.id) {
                        val updatedSong = currentSongValue.copy(albumArtPath = artPath)
                        _currentSong.value = updatedSong
                    }
                } else {
                    android.util.Log.d("ViewModel", "No album art found")
                }
            } catch (e: Exception) {
                android.util.Log.e("ViewModel", "Error searching album art: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    // Online music search - removed
    // fun searchOnlineMusic(query: String) { ... }
    
    // Download album art for song
    fun downloadAlbumArt(song: Song, imageUrl: String) {
        viewModelScope.launch {
            try {
                println("ViewModel: Starting download for ${song.title} from $imageUrl")
                val artPath = musicRepository.downloadAlbumArt(song, imageUrl)
                if (artPath != null) {
                    println("ViewModel: Download successful, updating UI")
                    // Force refresh the current song to update UI
                    val currentSongValue = _currentSong.value
                    if (currentSongValue?.id == song.id) {
                        val updatedSong = currentSongValue.copy(albumArtPath = artPath)
                        _currentSong.value = updatedSong
                        println("ViewModel: UI updated with new album art path")
                    }
                } else {
                    println("ViewModel: Download failed")
                }
            } catch (e: Exception) {
                println("ViewModel: Error downloading album art: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    // Equalizer functions
    fun setEqualizerPreset(preset: EqualizerPreset) {
        _equalizerBands.value = preset.bands
        _currentEqualizerPreset.value = preset.name
        applyEqualizer(preset.bands)
        saveEqualizerSettings()
    }
    
    fun setEqualizerBand(index: Int, value: Float) {
        val newBands = _equalizerBands.value.toMutableList()
        if (index < newBands.size) {
            newBands[index] = value
            _equalizerBands.value = newBands
            _currentEqualizerPreset.value = "自定义"
            applyEqualizer(newBands)
            saveEqualizerSettings()
        }
    }
    
    fun resetEqualizer() {
        val resetBands = listOf(0f, 0f, 0f, 0f, 0f)
        _equalizerBands.value = resetBands
        _currentEqualizerPreset.value = "正常"
        applyEqualizer(resetBands)
        saveEqualizerSettings()
    }
    
    private fun applyEqualizer(bands: List<Float>) {
        // Apply equalizer settings to ExoPlayer
        // This is a simplified implementation
    }
    
    private fun saveEqualizerSettings() {
        val bandsString = _equalizerBands.value.joinToString(",")
        prefs.edit()
            .putString("equalizer_bands", bandsString)
            .putString("equalizer_preset", _currentEqualizerPreset.value)
            .apply()
    }
    
    private fun loadEqualizerSettings() {
        val bandsString = prefs.getString("equalizer_bands", "0,0,0,0,0")
        val presetName = prefs.getString("equalizer_preset", "正常")
        
        bandsString?.let { str ->
            val bands = str.split(",").mapNotNull { it.toFloatOrNull() }
            if (bands.size == 5) {
                _equalizerBands.value = bands
            }
        }
        
        _currentEqualizerPreset.value = presetName ?: "正常"
    }
    
    private fun saveLastPlayedSong(song: Song) {
        prefs.edit()
            .putString("last_song_id", song.id)
            .putString("last_song_path", song.path)
            .putLong("last_position", exoPlayer.currentPosition)
            .apply()
    }
    
    private fun loadLastPlayedSong() {
        val lastSongPath = prefs.getString("last_song_path", null)
        val lastPosition = prefs.getLong("last_position", 0L)
        val playModeStr = prefs.getString("play_mode", PlayMode.SEQUENTIAL.name)
        val sortOrderStr = prefs.getString("sort_order", SortOrder.DATE_ADDED.name)
        
        _playMode.value = try {
            PlayMode.valueOf(playModeStr ?: PlayMode.SEQUENTIAL.name)
        } catch (e: Exception) {
            PlayMode.SEQUENTIAL
        }
        
        _sortOrder.value = try {
            SortOrder.valueOf(sortOrderStr ?: SortOrder.DATE_ADDED.name)
        } catch (e: Exception) {
            SortOrder.DATE_ADDED
        }
        
        if (lastSongPath != null) {
            viewModelScope.launch {
                // 只收集一次，避免重复触发
                val songList = songs.first()
                val lastSong = songList.find { it.path == lastSongPath }
                if (lastSong != null) {
                    _currentSong.value = lastSong
                    updateCurrentIndex(lastSong)
                    val mediaItem = MediaItem.fromUri(lastSong.path)
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.seekTo(lastPosition)
                    // Don't auto-play, just prepare
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}