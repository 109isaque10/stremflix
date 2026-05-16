package com.stremflix.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.stremflix.ui.R
import com.stremflix.ui.theme.NetflixRed
import com.stremflix.ui.theme.NetflixTextPrimary
import kotlinx.coroutines.delay

@Composable
fun CustomPlayerControls(
    player: Player,
    uiState: PlaybackUiState,
    contentTitle: String,
    contentSynopsis: String?,
    viewModel: PlaybackViewModel,
    season: Int? = null,
    episode: Int? = null,
    progress: Float,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }

    // Update progress/timer
    LaunchedEffect(player) {
        while (true) {
            currentPosition = player.currentPosition
            duration = player.duration
            delay(500)
        }
    }

    // Auto-hide controls
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            if (uiState is PlaybackUiState.Playing) isVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { isVisible = !isVisible }
                )
            }
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = modifier.fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { isVisible = !isVisible }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize().drawWithContent {
                // Smooth Netflix-style dark gradients at top and bottom
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                        startY = 0f,
                        endY = 300f
                    )
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                        startY = size.height - 350f,
                        endY = size.height
                    )
                )
            }) {
                if (uiState !is PlaybackUiState.Playing) {
                    InfoOverlay(
                        contentTitle = contentTitle,
                        contentSynopsis = contentSynopsis,
                        season = season,
                        episode = episode,
                        onDismiss = onPlayPause
                    )
                }

                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(id = R.string.playback_back),
                            tint = NetflixTextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (season != null && episode != null) "$contentTitle: S$season:E$episode" else contentTitle,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                // Center: Play/Pause & Buffering
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip Back 10s
                    IconButton(
                        onClick = { viewModel.skipBackward(10000); isVisible = true },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_rotate_left),
                            "Skip back 10s",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Text("10", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }

                    if (uiState is PlaybackUiState.Buffering) {
                        CircularProgressIndicator(color = NetflixRed, modifier = Modifier.size(72.dp), strokeWidth = 4.dp)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null // removes ripple for a cleaner look
                                ) {
                                    onPlayPause()
                                    isVisible = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState is PlaybackUiState.Playing)
                                    ImageVector.vectorResource(R.drawable.ic_pause)
                                else
                                    ImageVector.vectorResource(R.drawable.ic_play),
                                contentDescription = stringResource(id = R.string.playback_play_pause),
                                tint = NetflixTextPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    // Skip Forward 10s
                    IconButton(
                        onClick = { viewModel.skipForward(10000); isVisible = true},
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_rotate_right),
                            "Skip forward 10s",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Text("10", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }

                // Bottom Bar: Seek, Time, Duration
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 24.dp)
                ) {
                    val safeDuration = if (duration > 0L) duration.toFloat() else 100f
                    val safePosition = currentPosition.toFloat().coerceIn(0f, safeDuration)

                    // Seek Bar
                    Slider(
                        value = safePosition,
                        onValueChange = { ratio ->
                            if (duration > 0) viewModel.onSeekTo(ratio.toLong())
                        },
                        valueRange = 0f..safeDuration,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = NetflixRed,
                            activeTrackColor = NetflixRed,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )

                    // Time Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            color = NetflixTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Skip Intro / Next Episode buttons could go here
                        Text(
                            text = formatTime(duration),
                            color = NetflixTextPrimary.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Bottom Row Buttons
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp, vertical = 24.dp)
                    ) {
                        // Action Buttons (Episodes, Audio/Subs, Next Ep)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center, // Center aligns the buttons
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (season != null) {
                                TextButton(onClick = { /* Show episodes */ }) {
                                    Icon(ImageVector.vectorResource(R.drawable.ic_stack), null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Episodes", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.width(32.dp))
                            }

                            TextButton(onClick = { /* Show audio/subs */ }) {
                                Icon(ImageVector.vectorResource(R.drawable.ic_subtitles), null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Audio & Subtitles", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }

                            if (season != null) {
                                Spacer(Modifier.width(32.dp))
                                TextButton(onClick = viewModel::playNext) {
                                    Icon(ImageVector.vectorResource(R.drawable.ic_skip_next), null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Next Episode", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Progress Bar & Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = formatTime(currentPosition),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            val safeDuration = if (duration > 0L) duration.toFloat() else 100f
                            val progressRatio = currentPosition.toFloat().coerceIn(0f, 1f)

                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp) // Very thin, Netflix style
                                    .pointerInput(Unit) {
                                        detectTapGestures { offset ->
                                            if (duration > 0L) {
                                                // Calculate the percentage of where the user tapped on the bar
                                                val tapPercentage = (offset.x / size.width).coerceIn(0f, 1f)
                                                val newPosition = (tapPercentage * duration).toLong()
                                                viewModel.onSeekTo(newPosition)
                                            }
                                        }
                                    },
                                color = NetflixRed,
                                trackColor = Color.White.copy(alpha = 0.3f),
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )

                            // Netflix shows remaining time, but duration is fine too
                            Text(
                                text = formatTime(duration),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun InfoOverlay(
    contentTitle: String,
    contentSynopsis: String?,
    season: Int?,
    episode: Int?,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "You're watching",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = contentTitle,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )

            if (season != null && episode != null) {
                Text(
                    text = "Season ${season}: Ep. ${episode}",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(16.dp))

            if (!contentSynopsis.isNullOrBlank()) {
                Text(
                    text = contentSynopsis,
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Paused",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}