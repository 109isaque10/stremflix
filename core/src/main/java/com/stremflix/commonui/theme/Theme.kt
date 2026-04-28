package com.stremflix.commonui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NetflixRed,
    background = Black,
    surface = DarkGray,
    onPrimary = White,
    onBackground = White,
    onSurface = White,
    tertiary = GrayText
)

@Composable
fun StreamFlixTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography, // Omitted for brevity, assume standard Roboto setup
        content = content
    )
}