package com.stremflix.ui.player

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ContentRatingPopup(
    rating: String?,
    advisories: List<String>,
    modifier: Modifier = Modifier
) {
    // Only show if we actually have data to display
    if (rating == null && advisories.isEmpty()) return

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1000) // Wait 1 second after playback starts before sliding in
        isVisible = true
        delay(7000) // Keep it on screen for 7 seconds
        isVisible = false
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(600)) + slideInHorizontally(tween(600), initialOffsetX = { -it }),
        exit = fadeOut(tween(600)) + slideOutHorizontally(tween(600), targetOffsetX = { -it }),
        modifier = modifier
            .padding(top = 40.dp) // Pushed slightly down from the absolute top edge
            .wrapContentSize(Alignment.TopStart)
    ) {
        Row(
            modifier = Modifier
                .background(
                    color = Color.Black.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp) // Flush flat against the left screen edge
                )
                .height(IntrinsicSize.Min) // Forces the Row to bound its height to the children
                .padding(end = 24.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(Color.White)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                // Age Rating Box (e.g., "TV-MA")
                if (rating != null) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.White, RoundedCornerShape(2.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = rating,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Advisory List
                advisories.forEach { advisory ->
                    Text(
                        text = advisory,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}