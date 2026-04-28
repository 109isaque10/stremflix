package com.stremflix.tv.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
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
    var showControls by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    controllerAutoShow = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                    keepScreenOn = true
                    
                    player.initialize(this, findViewById<SubtitleView>(androidx.media3.ui.R.id.exo_subtitles))
                    player.loadMedia(streamUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
                if (showControls) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.TopStart).padding(32.dp)
            ) {
                Icon(
                    painter = androidx.compose.material.icons.Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            TvPlayerControls(
                modifier = Modifier.align(Alignment.BottomCenter),
                onPlayPause = { if (player.isPlaying()) player.pause() else player.play() },
                onSkipBack = { player.skipBack() },
                onSkipForward = { player.skipForward() },
                isPlaying = player.isPlaying()
            )
        }
    }
    
    // Show controls on key press
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            if (player.isPlaying()) {
                showControls = false
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }
}

@Composable
fun TvPlayerControls(
    modifier: Modifier = Modifier,
    onPlayPause: () -> Unit,
    onSkipBack: () -> Unit,
    onSkipForward: () -> Unit,
    isPlaying: Boolean
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(32.dp),        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.tv.material3.Button(onClick = onSkipBack) {
            Icon(
                painter = androidx.compose.material.icons.Icons.Default.Replay10,
                contentDescription = "Skip Back 10s"
            )
        }
        
        androidx.tv.material3.Button(
            onClick = onPlayPause,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                painter = if (isPlaying)
                    androidx.compose.material.icons.Icons.Default.Pause
                else
                    androidx.compose.material.icons.Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play"
            )
        }
        
        androidx.tv.material3.Button(onClick = onSkipForward) {
            Icon(
                painter = androidx.compose.material.icons.Icons.Default.Forward10,
                contentDescription = "Skip Forward 10s"
            )
        }
    }
}