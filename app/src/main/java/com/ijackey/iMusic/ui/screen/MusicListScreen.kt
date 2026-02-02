package com.ijackey.iMusic.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ijackey.iMusic.data.model.PlayMode
import com.ijackey.iMusic.data.model.Song
import com.ijackey.iMusic.data.model.SortOrder
import com.ijackey.iMusic.ui.viewmodel.MusicPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicListScreen(
    viewModel: MusicPlayerViewModel,
    onSongClick: (Song) -> Unit,
    onLyricsClick: (Song) -> Unit,
    onDeleteClick: (Song) -> Unit
) {
    val songs by viewModel.songs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    
    var showSortMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var songToDelete by remember { mutableStateOf<Song?>(null) }
    val listState = rememberLazyListState()
    
    // 自动定位到当前播放歌曲
    LaunchedEffect(currentSong, songs) {
        currentSong?.let { song ->
            val index = songs.indexOf(song)
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("iMusic") },
            actions = {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Sort")
                }
                IconButton(onClick = { viewModel.scanMusic() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Scan Music")
                }
                
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOrder.values().forEach { order ->
                        DropdownMenuItem(
                            text = { Text(order.name.replace("_", " ")) },
                            onClick = {
                                viewModel.setSortOrder(order)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        )
        
        // Play Control Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.continueLastPlayback() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("继续播放")
            }
            
            Button(
                onClick = { viewModel.startFromBeginning() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("从头开始")
            }
        }
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::setSearchQuery,
            label = { Text("搜索歌曲...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        
        // Songs Summary
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "共 ${songs.size} 首歌曲",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "总时长: ${formatTotalDuration(songs.sumOf { it.duration })}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Song List
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(songs.size) { index ->
                val song = songs[index]
                SongItem(
                    song = song,
                    index = index + 1,
                    isCurrentSong = song == currentSong,
                    isPlaying = isPlaying && song == currentSong,
                    onClick = { onSongClick(song) },
                    onLyricsClick = { onLyricsClick(song) },
                    onDeleteClick = { 
                        songToDelete = song
                        showDeleteDialog = true
                    },
                    hasLyrics = viewModel.getLyrics(song) != null
                )
            }
        }
    }
    
    DeleteConfirmationDialog(
        showDialog = showDeleteDialog,
        songTitle = songToDelete?.title ?: "",
        onConfirm = {
            songToDelete?.let { onDeleteClick(it) }
            showDeleteDialog = false
            songToDelete = null
        },
        onDismiss = {
            showDeleteDialog = false
            songToDelete = null
        }
    )
}

@Composable
fun SongItem(
    song: Song,
    index: Int,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onLyricsClick: () -> Unit,
    onDeleteClick: () -> Unit,
    hasLyrics: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentSong) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentSong) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Song Index
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrentSong && isPlaying) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCurrentSong) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Album Art
            Card(
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                if (song.albumArtPath != null || song.albumArt != null) {
                    coil.compose.AsyncImage(
                        model = song.albumArtPath ?: song.albumArt,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCurrentSong) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${formatDuration(song.duration)} • ${formatFileSize(song.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Action buttons
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasLyrics) {
                    IconButton(
                        onClick = onLyricsClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Face,
                            contentDescription = "歌词",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun formatDuration(duration: Long): String {
    val minutes = (duration / 1000) / 60
    val seconds = (duration / 1000) % 60
    return String.format("%d:%02d", minutes, seconds)
}

private fun formatTotalDuration(totalDuration: Long): String {
    val hours = (totalDuration / 1000) / 3600
    val minutes = ((totalDuration / 1000) % 3600) / 60
    return if (hours > 0) {
        String.format("%d小时%d分钟", hours, minutes)
    } else {
        String.format("%d分钟", minutes)
    }
}

private fun formatFileSize(size: Long): String {
    return when {
        size >= 1024 * 1024 -> String.format("%.1fMB", size / (1024.0 * 1024.0))
        size >= 1024 -> String.format("%.1fKB", size / 1024.0)
        else -> "${size}B"
    }
}

// Delete confirmation dialog
@Composable
fun DeleteConfirmationDialog(
    showDialog: Boolean,
    songTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("删除歌曲") },
            text = { Text("确定要删除《$songTitle》吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        )
    }
}