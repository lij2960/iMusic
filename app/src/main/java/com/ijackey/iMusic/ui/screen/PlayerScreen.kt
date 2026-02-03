package com.ijackey.iMusic.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.ijackey.iMusic.R
import com.ijackey.iMusic.ui.components.CustomSlider
import com.ijackey.iMusic.ui.components.SimpleProgressBar
import com.ijackey.iMusic.ui.components.CustomProgressBar
import coil.compose.AsyncImage
import com.ijackey.iMusic.data.model.PlayMode
import com.ijackey.iMusic.ui.viewmodel.MusicPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: MusicPlayerViewModel,
    onLyricsClick: (com.ijackey.iMusic.data.model.Song) -> Unit,
    onEqualizerClick: () -> Unit = {}
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val playMode by viewModel.playMode.collectAsState()
    
    // 选择对话框状态
    var showLyricsDialog by remember { mutableStateOf(false) }
    var showAlbumArtDialog by remember { mutableStateOf(false) }
    var lyricsOptions by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var albumArtOptions by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    
    // 获取歌词
    val lyrics by remember(currentSong) {
        derivedStateOf {
            currentSong?.let { song ->
                val lyricsText = viewModel.getLyrics(song)
                if (lyricsText != null) {
                    parsePlayerLyrics(lyricsText)
                } else {
                    emptyList()
                }
            } ?: emptyList()
        }
    }
    
    // 当前歌词行
    val currentLyricIndex = remember(currentPosition, lyrics) {
        if (lyrics.isEmpty()) -1
        else {
            var index = -1
            for (i in lyrics.indices) {
                if (currentPosition >= lyrics[i].timeMs) {
                    index = i
                } else {
                    break
                }
            }
            index
        }
    }
    
    val currentLyricText = if (currentLyricIndex >= 0 && currentLyricIndex < lyrics.size) {
        lyrics[currentLyricIndex].text
    } else {
        "♪ 暂无歌词 ♪"
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Song Info
        currentSong?.let { song ->
            Spacer(modifier = Modifier.height(32.dp))
            
            // Album Art with Coil
            val albumArtPath by remember(currentSong) {
                derivedStateOf {
                    currentSong?.albumArtPath ?: currentSong?.albumArt
                }
            }
            
            Card(
                modifier = Modifier
                    .size(180.dp)
                    .clip(MaterialTheme.shapes.large),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (albumArtPath != null) {
                        AsyncImage(
                            model = albumArtPath,
                            contentDescription = "Album Art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // 使用应用图标作为默认封面
                        AsyncImage(
                            model = R.drawable.default_album_art,
                            contentDescription = "Default Album Art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Song Title and Artist
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = song.artist,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 当前歌词显示
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickable { onLyricsClick(song) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentLyricText,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            Column {
                var isDragging by remember { mutableStateOf(false) }
                var dragProgress by remember { mutableStateOf(0f) }
                var lastSeekTime by remember { mutableStateOf(0L) }
                
                val displayProgress = if (isDragging || (System.currentTimeMillis() - lastSeekTime < 500)) {
                    dragProgress
                } else {
                    if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
                }
                
                CustomProgressBar(
                    progress = displayProgress,
                    onProgressChange = { progress ->
                        isDragging = true
                        dragProgress = progress
                    },
                    onProgressChangeFinished = {
                        val seekPosition = (dragProgress * duration).toLong()
                        viewModel.seekTo(seekPosition)
                        lastSeekTime = System.currentTimeMillis()
                        isDragging = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isDragging || (System.currentTimeMillis() - lastSeekTime < 500)) {
                            formatTime((dragProgress * duration).toLong())
                        } else {
                            formatTime(currentPosition)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTime(duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Control Buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play Mode Button
                    IconButton(
                        onClick = {
                            val nextMode = when (playMode) {
                                PlayMode.SEQUENTIAL -> PlayMode.SHUFFLE
                                PlayMode.SHUFFLE -> PlayMode.REPEAT_ONE
                                PlayMode.REPEAT_ONE -> PlayMode.SEQUENTIAL
                            }
                            viewModel.setPlayMode(nextMode)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                when (playMode) {
                                    PlayMode.SEQUENTIAL -> R.drawable.ic_repeat
                                    PlayMode.SHUFFLE -> R.drawable.ic_shuffle
                                    PlayMode.REPEAT_ONE -> R.drawable.ic_repeat_one
                                }
                            ),
                            contentDescription = "Play Mode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    // Previous Button
                    IconButton(
                        onClick = { viewModel.skipToPrevious() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_previous),
                            contentDescription = "Previous",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Play/Pause Button
                    FloatingActionButton(
                        onClick = { viewModel.playPause() },
                        modifier = Modifier.size(72.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    // Next Button
                    IconButton(
                        onClick = { viewModel.skipToNext() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_next),
                            contentDescription = "Next",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Equalizer Button
                    IconButton(
                        onClick = onEqualizerClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings),
                            contentDescription = "Equalizer",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Second row with cover and lyrics update
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Update Cover Button
                FilledTonalButton(
                    onClick = { 
                        android.util.Log.d("PlayerScreen", "Update cover button clicked")
                        currentSong?.let { currentPlayingSong ->
                            android.util.Log.d("PlayerScreen", "Searching cover for: ${currentPlayingSong.title} by ${currentPlayingSong.artist}")
                            viewModel.searchMultipleAlbumArt(currentPlayingSong) { options ->
                                if (options.size > 1) {
                                    albumArtOptions = options
                                    showAlbumArtDialog = true
                                } else if (options.size == 1) {
                                    viewModel.applySelectedAlbumArt(currentPlayingSong, options[0].second)
                                } else {
                                    viewModel.searchOnlineAlbumArt(currentPlayingSong)
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("更新封面")
                }
                
                // Update Lyrics Button
                FilledTonalButton(
                    onClick = { 
                        android.util.Log.d("PlayerScreen", "Update lyrics button clicked")
                        currentSong?.let { currentPlayingSong ->
                            android.util.Log.d("PlayerScreen", "Searching lyrics for: ${currentPlayingSong.title} by ${currentPlayingSong.artist}")
                            viewModel.searchMultipleLyrics(currentPlayingSong) { options ->
                                if (options.size > 1) {
                                    lyricsOptions = options
                                    showLyricsDialog = true
                                } else if (options.size == 1) {
                                    viewModel.applySelectedLyrics(currentPlayingSong, options[0].second)
                                } else {
                                    viewModel.searchOnlineLyrics(currentPlayingSong)
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("更新歌词")
                }
            }
        } ?: run {
            // No song selected
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "没有选择歌曲",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // 歌词选择对话框
    if (showLyricsDialog) {
        AlertDialog(
            onDismissRequest = { showLyricsDialog = false },
            title = { Text("选择歌词") },
            text = {
                LazyColumn {
                    items(lyricsOptions) { (displayName, lyrics) ->
                        TextButton(
                            onClick = {
                                currentSong?.let { song ->
                                    viewModel.applySelectedLyrics(song, lyrics)
                                }
                                showLyricsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = displayName,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLyricsDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 封面选择对话框
    if (showAlbumArtDialog) {
        AlertDialog(
            onDismissRequest = { showAlbumArtDialog = false },
            title = { Text("选择封面") },
            text = {
                LazyColumn {
                    items(albumArtOptions) { (displayName, imageUrl) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    currentSong?.let { song ->
                                        viewModel.applySelectedAlbumArt(song, imageUrl)
                                    }
                                    showAlbumArtDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 封面预览图片
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Album Art Preview",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                // 显示名称
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAlbumArtDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

data class PlayerLyricLine(
    val timeMs: Long,
    val text: String
)

private fun parsePlayerLyrics(lyricsText: String): List<PlayerLyricLine> {
    val lines = lyricsText.split("\n")
    val lyricLines = mutableListOf<PlayerLyricLine>()
    
    for (line in lines) {
        val trimmedLine = line.trim()
        if (trimmedLine.isEmpty()) continue
        
        // Parse LRC format: [mm:ss.xx]lyrics or [mm:ss]lyrics
        val timeRegex = Regex("\\[(\\d{2}):(\\d{2})(?:\\.(\\d{2}))?\\](.*)")
        val matchResult = timeRegex.find(trimmedLine)
        
        if (matchResult != null) {
            val minutes = matchResult.groupValues[1].toIntOrNull() ?: 0
            val seconds = matchResult.groupValues[2].toIntOrNull() ?: 0
            val centiseconds = matchResult.groupValues[3].toIntOrNull() ?: 0
            val text = matchResult.groupValues[4].trim()
            
            if (text.isNotEmpty()) {
                val timeMs = (minutes * 60 + seconds) * 1000L + centiseconds * 10L
                lyricLines.add(PlayerLyricLine(timeMs, text))
            }
        }
    }
    
    return lyricLines.sortedBy { it.timeMs }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}