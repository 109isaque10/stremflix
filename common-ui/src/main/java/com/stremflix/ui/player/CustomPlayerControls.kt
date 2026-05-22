package com.stremflix.ui.player

import androidx.annotation.OptIn
import androidx.appcompat.view.ContextThemeWrapper
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.TrackSelectionDialogBuilder
import com.stremflix.ui.R
import com.stremflix.ui.theme.NetflixRed
import com.stremflix.ui.theme.NetflixTextPrimary
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun CustomPlayerControls(
    player: Player,
    uiState: PlaybackUiState,
    contentTitle: String,
    contentSynopsis: String?,
    viewModel: PlaybackViewModel,
    isTvMode: Boolean,
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
    var showExtendedInfo by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val themedContext = remember(context) { ContextThemeWrapper(context, 		androidx.appcompat.R.style.Theme_AppCompat_DayNight_Dialog_Alert) }

    LaunchedEffect(uiState) {
        if (uiState !is PlaybackUiState.Playing) {
            // If paused, wait 10 seconds, then show the info
            delay(10000L)
            showExtendedInfo = true
        } else {
            // If playing, instantly hide the info and reset the timer
            showExtendedInfo = false
        }
    }

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
                        colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent),
                        startY = 0f,
                        endY = 200f
                    )
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                        startY = size.height - 250f,
                        endY = size.height
                    )
                )
            }) {
                AnimatedVisibility(
                    visible = showExtendedInfo,
                    enter = fadeIn(), exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    InfoOverlay(contentTitle, contentSynopsis, season, episode, onPlayPause)
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
                            modifier = Modifier.size(32.dp)
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
                        .padding(horizontal = 48.dp)
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip Back 10s
                    if(!isTvMode){
                        AnimatedVisibility(visible = !showExtendedInfo) {
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
                                Spacer(modifier = Modifier.width(32.dp))
                            }
                        }
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
                    if(!isTvMode){
                        AnimatedVisibility(visible = !showExtendedInfo) {
                            IconButton(
                                onClick = { viewModel.skipForward(10000); isVisible = true },
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
                    }
                }

                // Bottom Bar: Seek, Time, Duration
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 24.dp)
                ) {
                    // Bottom Row Buttons
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp, vertical = 24.dp)
                    ) {
                        // Action Buttons (Episodes, Audio/Subs, Next Ep)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End, // Center aligns the buttons
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

                            // ✅ 3. Native ExoPlayer Audio Track Selector
                            TextButton(onClick = {
                                TrackSelectionDialogBuilder(themedContext, "Audio", player, C.TRACK_TYPE_AUDIO).build().show()
                            }) {
                                Icon(ImageVector.vectorResource(R.drawable.ic_subtitles), null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Audio", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(Modifier.width(16.dp))

                            // ✅ 4. Native ExoPlayer Subtitle Track Selector
                            TextButton(onClick = {
                                TrackSelectionDialogBuilder(themedContext, "Subtitles", player, C.TRACK_TYPE_TEXT).build().show()
                            }) {
                                Icon(ImageVector.vectorResource(R.drawable.ic_list), null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Subtitles", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
                            val progressRatio = (currentPosition.toFloat() / safeDuration).coerceIn(0f, 1f)
                            val remainingTime = duration-currentPosition

                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp) // Very thin, Netflix style
                                    .pointerInput(Unit) {
                                        detectTapGestures { offset ->
                                            if (safeDuration > 0L) {
                                                // Calculate the percentage of where the user tapped on the bar
                                                val tapPercentage = (offset.x / size.width).coerceIn(0f, 1f)
                                                val newPosition = (tapPercentage * safeDuration).toLong()
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
                                text = formatTime(if (remainingTime > 0) remainingTime else duration),
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
            .fillMaxWidth(0.5f)
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(start = 48.dp, top = 90.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            },
        contentAlignment = Alignment.TopStart
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
