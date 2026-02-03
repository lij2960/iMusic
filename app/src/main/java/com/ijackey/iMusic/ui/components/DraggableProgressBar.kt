package com.ijackey.iMusic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun DraggableProgressBar(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var componentSize by remember { mutableStateOf(IntSize.Zero) }
    var dragProgress by remember { mutableStateOf(progress) }
    var isDragging by remember { mutableStateOf(false) }
    
    // Use drag progress when dragging, otherwise use actual progress
    val displayProgress = if (isDragging) dragProgress else progress
    
    Box(
        modifier = modifier
            .height(48.dp)
            .onGloballyPositioned { coordinates ->
                componentSize = coordinates.size
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val newProgress = (offset.x / componentSize.width).coerceIn(0f, 1f)
                        dragProgress = newProgress
                        onProgressChange(newProgress)
                    },
                    onDrag = { _, dragAmount ->
                        if (componentSize.width > 0) {
                            val deltaProgress = dragAmount.x / componentSize.width
                            dragProgress = (dragProgress + deltaProgress).coerceIn(0f, 1f)
                            onProgressChange(dragProgress)
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                    }
                )
            }
    ) {
        // Background track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.3f))
                .align(Alignment.Center)
        )
        
        // Active track
        Box(
            modifier = Modifier
                .fillMaxWidth(displayProgress.coerceIn(0f, 1f))
                .height(4.dp)
                .clip(CircleShape)
                .background(Color(0xFF6200EE))
                .align(Alignment.CenterStart)
        )
        
        // Thumb - using Canvas to avoid any system styling
        Canvas(
            modifier = Modifier
                .size(16.dp)
                .offset(
                    x = with(density) {
                        ((displayProgress.coerceIn(0f, 1f) * (componentSize.width - 16.dp.toPx())).toDp())
                    }
                )
                .align(Alignment.CenterStart)
        ) {
            drawCircle(
                color = Color(0xFF6200EE),
                radius = 8.dp.toPx()
            )
        }
    }
}