package com.stremflix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Vertical fade gradient applied strictly between text/metadata and backdrop/video.
 * Transitions from transparent at top to semi-transparent black at bottom.
 */
@Composable
fun VerticalFadeOverlay(
    modifier: Modifier = Modifier,
    topAlpha: Float = 0f,
    bottomAlpha: Float = 0.85f,
    color: Color = Color.Black,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = topAlpha),
                        color.copy(alpha = bottomAlpha)
                    ),
                    startY = 0f,
                    endY = size.height
                )
            )
        }
    ) {
        content()
    }
}