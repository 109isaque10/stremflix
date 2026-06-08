package com.stremflix.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(UnstableApi::class)
@Composable
fun TrailerPlayerScreen(
    trailerId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    keepScreenOn: Boolean = true
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current

    // Keep player state
    var youTubePlayerRef by remember { mutableStateOf<YouTubePlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var durationSec by remember { mutableStateOf(0f) }
    var currentSec by remember { mutableStateOf(0f) }
    var controlsVisible by remember { mutableStateOf(true) }

    // Force landscape + immersive while in trailer
    LaunchedEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // hide system bars (PlaybackScreen does similar). Not repeated here to keep snippet short.
    }
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Back handling
    BackHandler { onBack() }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        // YouTube player AndroidView
        AndroidView(
            factory = { ctx ->
                YouTubePlayerView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    lifecycleOwner.lifecycle.addObserver(this)
                    addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            youTubePlayerRef = youTubePlayer
                            // video duration may be reported later via listener
                            try {
                                if (autoPlay) youTubePlayer.loadVideo(trailerId, 0f) else youTubePlayer.cueVideo(trailerId, 0f)
                            } catch (t: Throwable) {
                                // ignore, library sometimes throws if invalid id
                            }
                        }

                        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                            isPlaying = state == PlayerConstants.PlayerState.PLAYING
                            if (state == PlayerConstants.PlayerState.ENDED) {
                                isPlaying = false
                            }
                        }

                        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                            currentSec = second
                        }

                        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                            durationSec = duration
                        }
                    })
                }
            },
            update = { /* no-op */ },
            modifier = Modifier.fillMaxSize()
        )

        // Minimal overlay scrim to ensure controls readable
        Box(modifier = Modifier.matchParentSize().drawWithContent {
            drawContent()
            drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.22f))), alpha = 1f)
        })

        // Controls overlay (auto-hide)
        LaunchedEffect(controlsVisible) {
            if (controlsVisible) {
                // auto hide after 4s
                val hideAfter = 4_000L
                var elapsed = 0L
                while (isActive && elapsed < hideAfter) {
                    delay(250L)
                    elapsed += 250L
                }
                controlsVisible = false
            }
        }

        // Toggle controls on tap
        Box(modifier = Modifier.matchParentSize().clickable { controlsVisible = !controlsVisible })

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .background(Color(0x22000000))
            ) {
                // Top row: back button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack) { Text("Back", color = Color.White) }
                    Spacer(Modifier.weight(1f))
                    // Play/Pause
                    IconButton(onClick = {
                        val p = youTubePlayerRef ?: return@IconButton
                        if (isPlaying) p.pause() else p.play()
                    }) {
                        Icon(
                            imageVector = if (isPlaying) com.stremflix.ui.R.drawable.ic_pause.let { androidx.compose.ui.graphics.vector.ImageVector.vectorResource(it) } else com.stremflix.ui.R.drawable.ic_play.let { androidx.compose.ui.graphics.vector.ImageVector.vectorResource(it) },
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Progress & time
                val safeDuration = if (durationSec > 0f) durationSec else 1f
                val progressFrac = (currentSec / safeDuration).coerceIn(0f, 1f)
                Text(text = "${formatTimeMs((currentSec*1000L).toLong())} / ${formatTimeMs((durationSec*1000L).toLong())}", color = Color.White)
                LinearProgressIndicator(progress = progressFrac, modifier = Modifier.fillMaxWidth().height(6.dp).progressSemantics(progressFrac), color = Color.Red)
            }
        }
    }
}

// Small helper to format ms -> mm:ss
private fun formatTimeMs(ms: Long): String {
    val totalSeconds = (ms / 1000)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}