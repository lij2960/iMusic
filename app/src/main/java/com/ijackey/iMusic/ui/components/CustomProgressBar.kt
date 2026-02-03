package com.ijackey.iMusic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun CustomProgressBar(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    onProgressChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    thumbColor: Color = MaterialTheme.colorScheme.primary
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf(progress) }
    
    val currentProgress = if (isDragging) dragProgress else progress
    
    val density = LocalDensity.current
    val trackHeight = 4.dp
    val thumbRadius = 8.dp
    
    Box(
        modifier = modifier
            .height(32.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        dragProgress = newProgress
                        onProgressChange(newProgress)
                    },
                    onDragEnd = {
                        isDragging = false
                        onProgressChangeFinished()
                    },
                    onDrag = { change, _ ->
                        val newProgress = ((change.position.x) / size.width).coerceIn(0f, 1f)
                        dragProgress = newProgress
                        onProgressChange(newProgress)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    dragProgress = newProgress
                    onProgressChange(newProgress)
                    onProgressChangeFinished()
                }
            }
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val trackY = size.height / 2
            val trackWidth = size.width
            val thumbX = currentProgress * trackWidth
            
            // 绘制背景轨道
            drawLine(
                color = inactiveColor,
                start = Offset(0f, trackY),
                end = Offset(trackWidth, trackY),
                strokeWidth = with(density) { trackHeight.toPx() },
                cap = StrokeCap.Round
            )
            
            // 绘制活动轨道
            if (currentProgress > 0f) {
                drawLine(
                    color = activeColor,
                    start = Offset(0f, trackY),
                    end = Offset(thumbX, trackY),
                    strokeWidth = with(density) { trackHeight.toPx() },
                    cap = StrokeCap.Round
                )
            }
            
            // 绘制拖动圆点
            drawCircle(
                color = thumbColor,
                radius = with(density) { thumbRadius.toPx() },
                center = Offset(thumbX, trackY)
            )
        }
    }
}