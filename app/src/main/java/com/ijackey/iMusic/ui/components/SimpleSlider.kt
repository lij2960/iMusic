package com.ijackey.iMusic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun SimpleProgressBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var size by remember { mutableStateOf(IntSize.Zero) }
    
    Box(
        modifier = modifier
            .height(40.dp)
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek(newProgress)
                    },
                    onDrag = { _, dragAmount ->
                        val currentProgress = progress + (dragAmount.x / size.width)
                        onSeek(currentProgress.coerceIn(0f, 1f))
                    }
                )
            }
    ) {
        // Background track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                .align(androidx.compose.ui.Alignment.Center)
        )
        
        // Active track
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
                .align(androidx.compose.ui.Alignment.CenterStart)
        )
        
        // Thumb
        Canvas(
            modifier = Modifier
                .size(12.dp)
                .align(androidx.compose.ui.Alignment.CenterStart)
        ) {
            val thumbX = (progress.coerceIn(0f, 1f) * (size.width - this.size.width)).coerceAtLeast(0f)
            drawCircle(
                color = Color(0xFF6200EE),
                radius = 6.dp.toPx(),
                center = Offset(thumbX + this.size.width / 2, this.size.height / 2)
            )
        }
    }
}

@Composable
fun SimpleSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = -10f..10f,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var size by remember { mutableStateOf(IntSize.Zero) }
    val normalizedValue = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
    
    Box(
        modifier = modifier
            .height(40.dp)
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val newNormalizedValue = (offset.x / size.width).coerceIn(0f, 1f)
                        val newValue = valueRange.start + newNormalizedValue * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newValue)
                    },
                    onDrag = { _, dragAmount ->
                        val currentNormalizedValue = normalizedValue + (dragAmount.x / size.width)
                        val clampedNormalizedValue = currentNormalizedValue.coerceIn(0f, 1f)
                        val newValue = valueRange.start + clampedNormalizedValue * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newValue)
                    }
                )
            }
    ) {
        // Background track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                .align(androidx.compose.ui.Alignment.Center)
        )
        
        // Zero line (center for -10 to +10 range)
        val zeroPosition = (-valueRange.start) / (valueRange.endInclusive - valueRange.start)
        if (value >= 0) {
            // Positive value: from center to current position
            Box(
                modifier = Modifier
                    .fillMaxWidth((normalizedValue - zeroPosition).coerceAtLeast(0f))
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .offset(x = with(density) { (zeroPosition * size.width).toDp() })
                    .align(androidx.compose.ui.Alignment.CenterStart)
            )
        } else {
            // Negative value: from current position to center
            Box(
                modifier = Modifier
                    .fillMaxWidth((zeroPosition - normalizedValue).coerceAtLeast(0f))
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .offset(x = with(density) { (normalizedValue * size.width).toDp() })
                    .align(androidx.compose.ui.Alignment.CenterStart)
            )
        }
        
        // Thumb
        Canvas(
            modifier = Modifier
                .size(12.dp)
                .align(androidx.compose.ui.Alignment.CenterStart)
        ) {
            val thumbX = (normalizedValue * (size.width - this.size.width)).coerceAtLeast(0f)
            drawCircle(
                color = Color(0xFF6200EE),
                radius = 6.dp.toPx(),
                center = Offset(thumbX + this.size.width / 2, this.size.height / 2)
            )
        }
    }
}