package com.stremflix.common.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFFE50914),
    onPrimary = Color.White,
    surface = Color(0xFF141414),
    onSurface = Color.White,
    background = Color(0xFF000000),
    onBackground = Color.White
)

private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFFE50914),
    onPrimary = Color.White,
    surface = Color(0xFFF2F2F2),
    onSurface = Color(0xFF111111),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111111)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
