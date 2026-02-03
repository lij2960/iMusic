package com.ijackey.iMusic.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ijackey.iMusic.ui.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.delay

data class LyricLine(
    val timeMs: Long,
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    songId: String,
    viewModel: MusicPlayerViewModel,
    onBack: () -> Unit
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    
    val lyrics = remember(currentSong) {
        currentSong?.let { song ->
            val lyricsText = viewModel.getLyrics(song)
            if (lyricsText != null) {
                parseLyrics(lyricsText)
            } else {
                emptyList()
            }
        } ?: emptyList()
    }
    
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
    
    val listState = rememberLazyListState()
    
    // Auto scroll to current lyric
    LaunchedEffect(currentLyricIndex) {
        if (currentLyricIndex >= 0 && lyrics.isNotEmpty()) {
            listState.animateScrollToItem(
                index = maxOf(0, currentLyricIndex - 2),
                scrollOffset = 0
            )
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { 
                Text(
                    text = currentSong?.title ?: "歌词",
                    maxLines = 1
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = { viewModel.playPause() }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放"
                    )
                }
            }
        )
        
        if (lyrics.isEmpty()) {
            // No lyrics available
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
                        text = "暂无歌词",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "请在音乐文件同目录下放置同名的.lrc文件",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Lyrics display
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(lyrics) { index, lyricLine ->
                    val isCurrentLine = index == currentLyricIndex
                    val isNearCurrentLine = kotlin.math.abs(index - currentLyricIndex) <= 1
                    
                    LyricLineItem(
                        lyric = lyricLine,
                        isCurrentLine = isCurrentLine,
                        isNearCurrentLine = isNearCurrentLine,
                        onClick = {
                            viewModel.seekTo(lyricLine.timeMs)
                        }
                    )
                }
                
                // Add some bottom padding
                item {
                    Spacer(modifier = Modifier.height(200.dp))
                }
            }
        }
    }
}

@Composable
fun LyricLineItem(
    lyric: LyricLine,
    isCurrentLine: Boolean,
    isNearCurrentLine: Boolean,
    onClick: () -> Unit
) {
    val alpha = when {
        isCurrentLine -> 1f
        isNearCurrentLine -> 0.8f
        else -> 0.5f
    }
    
    val textColor = if (isCurrentLine) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = lyric.text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = if (isCurrentLine) 18.sp else 16.sp,
                fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal
            ),
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp)
        )
    }
}

private fun parseLyrics(lyricsText: String): List<LyricLine> {
    val lines = lyricsText.split("\n")
    val lyricLines = mutableListOf<LyricLine>()
    
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
                lyricLines.add(LyricLine(timeMs, text))
            }
        }
    }
    
    return lyricLines.sortedBy { it.timeMs }
}