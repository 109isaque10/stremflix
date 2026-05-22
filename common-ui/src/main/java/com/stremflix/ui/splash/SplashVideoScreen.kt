package com.stremflix.ui.splash

import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.stremflix.ui.player.PlayerManager
import com.stremflix.ui.theme.NetflixBlack

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    playerManager: PlayerManager,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(NetflixBlack)) {
        SplashVideoLayer(
            playerManager = playerManager,
            onFinish = onNavigateToHome,
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun SplashVideoLayer(
    viewModel: SplashViewModel,
    playerManager: PlayerManager,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val alpha = remember { Animatable(1f) }
    val player = remember { playerManager.getPlayer() }
    val playbackState by playerManager.playbackState.collectAsState()

    LaunchedEffect(state) {
        if (state == SplashState.ReadyToPlay) {
            // Unpause the player!
            player.playWhenReady = true
        }
    }

    LaunchedEffect(playbackState) {
        if (playbackState == Player.STATE_ENDED) {
            viewModel.onVideoEnded()
            alpha.animateTo(0f, animationSpec = tween(durationMillis = 800))
            onFinish()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setBackgroundColor(android.graphics.Color.BLACK)

                player.playWhenReady = false
            }
        },
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha.value) // Smooth fade out when finished
    )
}