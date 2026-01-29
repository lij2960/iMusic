package com.ijackey.iMusic.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ijackey.iMusic.data.api.FangpiTrack
import com.ijackey.iMusic.ui.viewmodel.MusicPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FangpiSearchScreen(
    viewModel: MusicPlayerViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<FangpiTrack>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var downloadingTracks by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("搜索音乐") },
            placeholder = { Text("输入歌名或歌手") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (searchQuery.isNotBlank()) {
                            isSearching = true
                            viewModel.searchFangpiMusic(searchQuery) { results ->
                                searchResults = results
                                isSearching = false
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search results
        if (isSearching) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (searchResults.isNotEmpty()) {
            LazyColumn {
                items(searchResults) { track ->
                    FangpiTrackItem(
                        track = track,
                        isDownloading = downloadingTracks.contains(track.id),
                        onDownload = {
                            downloadingTracks = downloadingTracks + track.id
                            viewModel.downloadFangpiMusic(track) { success: Boolean ->
                                downloadingTracks = downloadingTracks - track.id
                                // 不管是直接下载还是跳转浏览器，都显示成功
                            }
                        }
                    )
                }
            }
        } else if (searchQuery.isNotBlank()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("未找到相关音乐")
            }
        }
    }
}

@Composable
fun FangpiTrackItem(
    track: FangpiTrack,
    isDownloading: Boolean,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            } else {
                IconButton(onClick = onDownload) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "下载",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}