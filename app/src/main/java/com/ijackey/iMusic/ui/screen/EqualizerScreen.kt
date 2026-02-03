package com.ijackey.iMusic.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ijackey.iMusic.data.model.EqualizerPresets
import com.ijackey.iMusic.ui.viewmodel.MusicPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    onBackClick: () -> Unit,
    viewModel: MusicPlayerViewModel = hiltViewModel()
) {
    val equalizerBands by viewModel.equalizerBands.collectAsState()
    val currentPreset by viewModel.currentEqualizerPreset.collectAsState()
    
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
                text = "均衡器",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Presets
        Text(
            text = "预设",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn(
            modifier = Modifier.height(200.dp)
        ) {
            items(EqualizerPresets.presets) { preset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentPreset == preset.name) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.surface
                    ),
                    onClick = { viewModel.setEqualizerPreset(preset) }
                ) {
                    Text(
                        text = preset.name,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Manual Controls
        Text(
            text = "手动调节",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        val frequencies = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz")
        
        Column {
            frequencies.forEachIndexed { index, freq ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = freq,
                        modifier = Modifier.width(80.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Slider(
                        value = equalizerBands.getOrNull(index) ?: 0f,
                        onValueChange = { value ->
                            viewModel.setEqualizerBand(index, value)
                        },
                        valueRange = -10f..10f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF6200EE),
                            activeTrackColor = Color(0xFF6200EE),
                            inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                    
                    Text(
                        text = "${(equalizerBands.getOrNull(index) ?: 0f).toInt()}dB",
                        modifier = Modifier.width(50.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Reset Button
        Button(
            onClick = { viewModel.resetEqualizer() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("重置")
        }
    }
}