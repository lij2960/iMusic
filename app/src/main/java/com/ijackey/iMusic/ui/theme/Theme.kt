package com.ijackey.iMusic.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TechAccent,
    secondary = SilverGray80,
    tertiary = TechBlue80,
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF2A2A2A),
    surfaceVariant = Color(0xFF3A3A3A),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = SilverGray80,
    onSurface = SilverGray80,
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF606060)
)

private val LightColorScheme = lightColorScheme(
    primary = TechBlue40,
    secondary = SilverGray40,
    tertiary = MetalGray40,
    background = TechLight,
    surface = Color.White,
    surfaceVariant = Color(0xFFEEEEEE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TechDark,
    onSurface = TechDark,
    onSurfaceVariant = Color(0xFF505050),
    outline = Color(0xFFB0B0B0)
)

@Composable
fun IMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 禁用动态颜色，使用自定义科技风格
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}