package com.stremflix.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NetflixRed,
    onPrimary = NetflixTextPrimary,
    secondary = NetflixSurfaceLight,
    onSecondary = NetflixTextSecondary,
    tertiary = NetflixSurface,
    background = NetflixBlack,
    surface = NetflixSurface,
    onSurface = NetflixTextPrimary,
    error = NetflixRed,
    onError = NetflixTextPrimary
)

@Composable
fun StremFlixTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = NetflixBlack.toArgb()
            window.navigationBarColor = NetflixBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = StremFlixTypography,
        content = content
    )
}