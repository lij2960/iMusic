package com.ijackey.iMusic.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ijackey.iMusic.ui.viewmodel.MusicPlayerViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DirectoryPickerScreen(
    viewModel: MusicPlayerViewModel,
    onDirectorySelected: () -> Unit
) {
    val context = LocalContext.current
    var currentPath by remember { mutableStateOf("/storage/emulated/0/") }
    var directories by remember { mutableStateOf<List<File>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Permission handling
    val storagePermission = rememberPermissionState(
        permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadDirectories(currentPath) { dirs ->
                directories = dirs
            }
        }
    }
    
    LaunchedEffect(currentPath) {
        if (storagePermission.status.isGranted) {
            loadDirectories(currentPath) { dirs ->
                directories = dirs
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("选择音乐目录") },
            navigationIcon = {
                IconButton(onClick = onDirectorySelected) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        isLoading = true
                        viewModel.scanMusicFromDirectory(currentPath)
                        isLoading = false
                        onDirectorySelected()
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "确认选择")
                }
            }
        )
        
        if (!storagePermission.status.isGranted) {
            // Permission request UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "需要存储权限来访问音乐文件",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "请授予存储权限以扫描和播放音乐",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                Manifest.permission.READ_MEDIA_AUDIO
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                        )
                    }
                ) {
                    Text("授予权限")
                }
            }
        } else {
            // Directory browser
            Column {
                // Current path
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentPath,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Quick access buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            currentPath = "/storage/emulated/0/Music/"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("音乐")
                    }
                    Button(
                        onClick = {
                            currentPath = "/storage/emulated/0/Download/"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("下载")
                    }
                    Button(
                        onClick = {
                            isLoading = true
                            viewModel.scanMusic()
                            isLoading = false
                            onDirectorySelected()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("全部扫描")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Directory list
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        // Parent directory
                        if (currentPath != "/") {
                            item {
                                DirectoryItem(
                                    name = "..",
                                    isDirectory = true,
                                    onClick = {
                                        val parent = File(currentPath).parent
                                        if (parent != null) {
                                            currentPath = parent
                                        }
                                    }
                                )
                            }
                        }
                        
                        items(directories) { directory ->
                            DirectoryItem(
                                name = directory.name,
                                isDirectory = directory.isDirectory,
                                onClick = {
                                    if (directory.isDirectory) {
                                        currentPath = directory.absolutePath
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DirectoryItem(
    name: String,
    isDirectory: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDirectory) Icons.Default.Add else Icons.Default.Star,
                contentDescription = null,
                tint = if (isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun loadDirectories(path: String, onResult: (List<File>) -> Unit) {
    try {
        val directory = File(path)
        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()?.filter { file ->
                file.isDirectory || isAudioFile(file)
            }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
            onResult(files)
        } else {
            onResult(emptyList())
        }
    } catch (e: Exception) {
        onResult(emptyList())
    }
}

private fun isAudioFile(file: File): Boolean {
    val audioExtensions = listOf("mp3", "wav", "flac", "aac", "ogg", "m4a")
    return audioExtensions.any { file.extension.lowercase() == it }
}