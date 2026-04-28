package com.stremflix.app.ui.screens.player

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView
import com.stremflix.commonui.player.StremFlixPlayer
import com.stremflix.commonui.theme.Black
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    streamUrl: String,
    viewModel: PlayerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val player = remember { StreamFlixPlayer(context, viewModel.getPlaybackSettings()) }
    var showControls by remember { mutableStateOf(true) }
    var dragOffset by remember { mutableStateOf(0f) }
    
    LaunchedEffect(streamUrl) {
        // PlayerView will be initialized in AndroidView
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    dragOffset += dragAmount
                    if (dragOffset > 100) {
                        player.skipBack()
                        dragOffset = 0f
                    } else if (dragOffset < -100) {
                        player.skipForward()                        dragOffset = 0f
                    }
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    awaitPointerEvent().changes.first().position
                    showControls = true
                    // Auto-hide controls after 3 seconds
                    while (true) {
                        delay(3000)
                        if (player.isPlaying()) {
                            showControls = false
                        }
                    }
                }
            }
    ) {
        // Player View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    controllerAutoShow = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                    keepScreenOn = true
                    
                    // Initialize player
                    player.initialize(this, findViewById<SubtitleView>(androidx.media3.ui.R.id.exo_subtitles))
                    player.loadMedia(streamUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Back Button
        if (showControls) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    painter = androidx.compose.material.icons.Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
                        // Custom Controls Overlay
            PlayerControls(
                modifier = Modifier.align(Alignment.BottomCenter),
                onPlayPause = {
                    if (player.isPlaying()) player.pause() else player.play()
                },
                onSkipBack = { player.skipBack() },
                onSkipForward = { player.skipForward() },
                onMuteToggle = { player.toggleMute() },
                isPlaying = player.isPlaying()
            )
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }
}

@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    onPlayPause: () -> Unit,
    onSkipBack: () -> Unit,
    onSkipForward: () -> Unit,
    onMuteToggle: () -> Unit,
    isPlaying: Boolean
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSkipBack) {
            Icon(
                painter = androidx.compose.material.icons.Icons.Default.Replay10,
                contentDescription = "Skip Back",
                tint = Color.White
            )
        }
        
        IconButton(onClick = onPlayPause) {
            Icon(
                painter = if (isPlaying)
                    androidx.compose.material.icons.Icons.Default.Pause                else
                    androidx.compose.material.icons.Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
        
        IconButton(onClick = onSkipForward) {
            Icon(
                painter = androidx.compose.material.icons.Icons.Default.Forward10,
                contentDescription = "Skip Forward",
                tint = Color.White
            )
        }
        
        IconButton(onClick = onMuteToggle) {
            Icon(
                painter = androidx.compose.material.icons.Icons.Default.VolumeUp,
                contentDescription = "Mute",
                tint = Color.White
            )
        }
    }
}