package com.stremflix.ui.splash

import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.stremflix.ui.R
import com.stremflix.ui.player.ExoPlayerView
import com.stremflix.ui.splash.SplashState
import com.stremflix.ui.player.PlayerManager
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixRed
import com.stremflix.ui.theme.NetflixTextSecondary
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    playerManager: PlayerManager,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    val shouldPlayVideo = state == SplashState.ReadyToPlay

    Box(modifier = modifier.fillMaxSize().background(NetflixBlack)) {
        // Video layer
        SplashVideoScreen(
            playerManager = playerManager,
            onFinish = onNavigateToHome,
            viewModel = viewModel,
            modifier = modifier
        )

        // Optional: Loading indicator while fetching content
        if (state == SplashState.WaitingForContent) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 48.dp)
                ) {
                    CircularProgressIndicator(color = NetflixRed)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Loading content...",
                        color = NetflixTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Error state
        if (state is SplashState.Error) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NetflixBlack.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_error),
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = (state as SplashState.Error).message,
                        color = NetflixTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { /* Retry or skip */ onNavigateToHome() },
                        colors = ButtonDefaults.buttonColors(containerColor = NetflixRed)
                    ) {
                        Text("Continue Anyway", color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun SplashVideoScreen(
    viewModel: SplashViewModel,
    playerManager: PlayerManager,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val alpha = remember { Animatable(1f) }
    val playbackState by playerManager.playbackState.collectAsState()

    val player = remember { playerManager.getPlayer() }

    LaunchedEffect(state) {
        if (state == SplashState.ReadyToPlay) {
            viewModel.startPlayback()
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
                setOnTouchListener { _, _ -> true }
            }
        },
        modifier = modifier.fillMaxSize()
    )

    // Show loading while waiting for content
    if (state == SplashState.WaitingForContent) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NetflixRed)
        }
    }
}