package com.stremflix.ui.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.stremflix.ui.R
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixRed
import com.stremflix.ui.theme.NetflixSurfaceLight
import com.stremflix.ui.theme.NetflixTextPrimary
import com.stremflix.ui.theme.NetflixTextSecondary

@Composable
fun UpNextModal(
    info: UpNextInfo?,
    countdownSeconds: Int,
    onPlayNow: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (info == null) return

    val progress = remember(countdownSeconds) { Animatable(1f) }

    LaunchedEffect(countdownSeconds) {
        progress.snapTo(1f)
        progress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Surface(
            color = NetflixSurfaceLight,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 8.dp,
            modifier = Modifier.widthIn(max = 400.dp)
        ) {
            Column {
                // Thumbnail & Info
                Row(modifier = Modifier.height(120.dp)) {
                    // Thumbnail
                    Box(modifier = Modifier.width(160.dp)) {
                        AsyncImage(
                            model = info.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Countdown Progress Bar Overlay
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .height(4.dp)
                                .fillMaxWidth(progress.value)
                                .background(NetflixRed)
                        )
                    }

                    // Text Info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.playback_up_next),
                                style = MaterialTheme.typography.labelMedium,
                                color = NetflixTextSecondary
                            )
                            Text(
                                text = info.seasonEpisode ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = NetflixTextSecondary
                            )
                            Text(
                                text = info.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = NetflixTextPrimary
                            )
                        }
                        Text(
                            text = stringResource(id = R.string.playback_auto_play_in, countdownSeconds),
                            style = MaterialTheme.typography.bodySmall,
                            color = NetflixTextSecondary
                        )
                    }
                }

                // Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(id = R.string.playback_dismiss),
                            color = NetflixTextSecondary
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onPlayNow,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_play),
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.playback_play_now),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}