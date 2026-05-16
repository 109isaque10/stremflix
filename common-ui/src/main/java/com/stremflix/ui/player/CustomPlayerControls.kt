package com.stremflix.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
    var buffered by remember { mutableStateOf(0) }

    // Update progress/timer
    LaunchedEffect(player) {
        while (true) {
            currentPosition = player.currentPosition
            duration = player.duration
            buffered = player.bufferedPercentage
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

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { isVisible = !isVisible }
            }
            .drawWithContent {
                drawContent()
                // Top & Bottom Gradients
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent),
                        startY = 0f,
                        endY = 200f
                    )
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = size.height - 250f,
                        endY = size.height
                    )
                )
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(16.dp),
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
                        text = contentTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (season != null && episode != null) {
                        Text(
                            text = "S${season}:E${episode}",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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
                    onClick = { viewModel.skipBackward(10000) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.ic_rotate_left),
                        "Skip back 10s",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Text("10", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }

                if (uiState is PlaybackUiState.Buffering) {
                    CircularProgressIndicator(color = NetflixRed, strokeWidth = 3.dp)
                } else {
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
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
                    onClick = { viewModel.skipForward(10000) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.ic_rotate_right),
                        "Skip forward 10s",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Text("10", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }

            // Bottom Bar: Seek, Time, Duration
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Episodes (for TV shows)
                    if (season != null) {
                        TextButton(onClick = { /* Show episodes dialog */ }) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_stack),
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Episodes", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Audio & Subtitles
                    TextButton(onClick = { /* Show audio/subs dialog */ }) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_subtitles),
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Audio & Subs", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }

                    Spacer(Modifier.width(16.dp))

                    // Next Episode (for TV shows)
                    TextButton(onClick = viewModel::playNext) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_skip_next),
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Next Ep.", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Info Overlay (after 15s pause)
        if (uiState !is PlaybackUiState.Playing) {
            InfoOverlay(
                contentTitle = contentTitle,
                contentSynopsis = contentSynopsis,
                season = season,
                episode = episode,
                onDismiss = { }
            )
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