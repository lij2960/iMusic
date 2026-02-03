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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun CustomSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.outline
) {
    val density = LocalDensity.current
    var sliderWidth by remember { mutableStateOf(0f) }
    
    Box(
        modifier = modifier
            .height(40.dp)
            .fillMaxWidth()
    ) {
        // Track background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(inactiveColor)
                .align(androidx.compose.ui.Alignment.Center)
        )
        
        // Active track
        val progress = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(activeColor)
                .align(androidx.compose.ui.Alignment.CenterStart)
        )
        
        // Thumb
        Canvas(
            modifier = Modifier
                .size(16.dp)
                .offset(x = with(density) { (sliderWidth * progress - 8.dp.toPx()).toDp() })
                .align(androidx.compose.ui.Alignment.CenterStart)
                .pointerInput(Unit) {
                    detectDragGestures { _, _ ->
                        // Drag handling will be implemented differently
                    }
                }
        ) {
            sliderWidth = size.width
            drawCircle(
                color = activeColor,
                radius = 8.dp.toPx()
            )
        }
    }
}