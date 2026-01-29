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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ijackey.iMusic.data.api.OnlineTrack
import com.ijackey.iMusic.ui.viewmodel.MusicPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineMusicScreen(
    onBackClick: () -> Unit,
    viewModel: MusicPlayerViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.onlineSearchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "在线音乐",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("搜索歌曲、艺术家") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(
                    onClick = { 
                        if (searchQuery.isNotBlank()) {
                            println("Online search button clicked: $searchQuery")
                            viewModel.searchOnlineMusic(searchQuery)
                        } else {
                            println("Search query is blank")
                        }
                    }
                ) {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                }
            },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Loading indicator
        if (isSearching) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Search Results
        LazyColumn {
            items(searchResults) { track ->
                OnlineTrackItem(
                    track = track,
                    onDownloadClick = { 
                        // Download functionality would be implemented here
                    },
                    onAlbumArtClick = { imageUrl ->
                        println("OnlineMusic: Album art download requested: $imageUrl")
                        // Use the first song from current playlist as target
                        val songs = viewModel.songs.value
                        if (songs.isNotEmpty()) {
                            // Try to find a matching song or use the first one
                            val targetSong = songs.find { 
                                it.title.contains(track.name, ignoreCase = true) ||
                                it.artist.contains(track.artists.firstOrNull()?.name ?: "", ignoreCase = true)
                            } ?: songs.first()
                            
                            println("OnlineMusic: Using target song: ${targetSong.title}")
                            viewModel.downloadAlbumArt(targetSong, imageUrl)
                        } else {
                            println("OnlineMusic: No songs available for album art download")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OnlineTrackItem(
    track: OnlineTrack,
    onDownloadClick: () -> Unit,
    onAlbumArtClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art
            Card(
                modifier = Modifier.size(60.dp),
                shape = MaterialTheme.shapes.small
            ) {
                AsyncImage(
                    model = track.album.artist.img1v1Url,
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    fallback = null
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Track Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artists.firstOrNull()?.name ?: "Unknown Artist",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.album.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Action Buttons
            Column {
                IconButton(
                    onClick = { 
                        val coverUrl = track.album.artist.img1v1Url
                        if (!coverUrl.isNullOrEmpty()) {
                            println("Download album art clicked: $coverUrl")
                            onAlbumArtClick(coverUrl)
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "下载封面",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onDownloadClick) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "试听",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

