package com.ijackey.iMusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ijackey.iMusic.ui.screen.MusicListScreen
import com.ijackey.iMusic.ui.screen.PlayerScreen
import com.ijackey.iMusic.ui.screen.DirectoryPickerScreen
import com.ijackey.iMusic.ui.screen.LyricsScreen
import com.ijackey.iMusic.ui.screen.EqualizerScreen
import com.ijackey.iMusic.ui.theme.IMusicTheme
import com.ijackey.iMusic.ui.viewmodel.MusicPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MusicPlayerViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IMusicTheme {
                viewModel = hiltViewModel()
                MusicPlayerApp(viewModel)
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (::viewModel.isInitialized) {
            viewModel.saveCurrentState()
        }
    }
    
    override fun onStop() {
        super.onStop()
        if (::viewModel.isInitialized) {
            viewModel.saveCurrentState()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerApp(viewModel: MusicPlayerViewModel) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("音乐库") },
                    selected = navController.currentDestination?.route == "music_list",
                    onClick = {
                        navController.navigate("music_list") {
                            popUpTo("music_list") { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    label = { Text("播放器") },
                    selected = navController.currentDestination?.route == "player",
                    onClick = {
                        navController.navigate("player") {
                            popUpTo("player") { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    label = { Text("导入") },
                    selected = navController.currentDestination?.route == "directory_picker",
                    onClick = {
                        navController.navigate("directory_picker")
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "music_list",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("music_list") {
                MusicListScreen(
                    viewModel = viewModel,
                    onSongClick = { song ->
                        viewModel.playSong(song)
                        navController.navigate("player")
                    },
                    onLyricsClick = { song ->
                        navController.navigate("lyrics/${song.id}")
                    }
                )
            }
            
            composable("player") {
                PlayerScreen(
                    viewModel = viewModel,
                    onLyricsClick = { song ->
                        navController.navigate("lyrics/${song.id}")
                    },
                    onEqualizerClick = {
                        navController.navigate("equalizer")
                    }
                )
            }
            
            composable("directory_picker") {
                DirectoryPickerScreen(
                    viewModel = viewModel,
                    onDirectorySelected = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("lyrics/{songId}") { backStackEntry ->
                val songId = backStackEntry.arguments?.getString("songId") ?: ""
                LyricsScreen(
                    songId = songId,
                    viewModel = viewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("equalizer") {
                EqualizerScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}