package com.bowlerspeed.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BowlerColorScheme = lightColorScheme(
    primary = Color(0xFF1F6F54),
    onPrimary = Color.White,
    secondary = Color(0xFFC4492D),
    onSecondary = Color.White,
    tertiary = Color(0xFF2E5AAC),
    background = Color(0xFFF7F8F5),
    onBackground = Color(0xFF17211B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF17211B),
    surfaceVariant = Color(0xFFE7ECE7),
    onSurfaceVariant = Color(0xFF4B5A52),
    outline = Color(0xFF7B8A82)
)

@Composable
fun BowlerSpeedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BowlerColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}