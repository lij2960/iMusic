package com.ijackey.iMusic.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.hilt.navigation.compose.hiltViewModel
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
                    .size(200.dp)
                    .clip(CircleShape),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                        // Show placeholder and search button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { 
                                    println("PlayerScreen: Searching album art for: ${song.artist} ${song.title}")
                                    // Search for album art only
                                    viewModel.searchOnlineAlbumArt(song)
                                }
                            ) {
                                Text("搜索封面")
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Song Title and Artist
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 当前歌词显示
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clickable { onLyricsClick(song) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentLyricText,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress Bar
            Column {
                Slider(
                    value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                    onValueChange = { progress ->
                        viewModel.seekTo((progress * duration).toLong())
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPosition),
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    }
                ) {
                    Icon(
                        imageVector = when (playMode) {
                            PlayMode.SEQUENTIAL -> Icons.Default.PlayArrow
                            PlayMode.SHUFFLE -> Icons.Default.Star
                            PlayMode.REPEAT_ONE -> Icons.Default.Refresh
                        },
                        contentDescription = "Play Mode",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Previous Button
                IconButton(
                    onClick = { viewModel.skipToPrevious() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Play/Pause Button
                FloatingActionButton(
                    onClick = { viewModel.playPause() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Clear else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Next Button
                IconButton(
                    onClick = { viewModel.skipToNext() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Equalizer Button
                IconButton(
                    onClick = onEqualizerClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Equalizer",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Second row with cover and lyrics update
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Update Cover Button
                Button(
                    onClick = { 
                        android.util.Log.d("PlayerScreen", "Update cover button clicked")
                        currentSong?.let { currentPlayingSong ->
                            android.util.Log.d("PlayerScreen", "Searching cover for: ${currentPlayingSong.title} by ${currentPlayingSong.artist}")
                            viewModel.searchOnlineAlbumArt(currentPlayingSong)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("更新封面")
                }
                
                // Update Lyrics Button
                Button(
                    onClick = { 
                        android.util.Log.d("PlayerScreen", "Update lyrics button clicked")
                        currentSong?.let { currentPlayingSong ->
                            android.util.Log.d("PlayerScreen", "Searching lyrics for: ${currentPlayingSong.title} by ${currentPlayingSong.artist}")
                            viewModel.searchOnlineLyrics(currentPlayingSong)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
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