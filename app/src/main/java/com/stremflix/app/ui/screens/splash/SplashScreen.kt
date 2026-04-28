package com.stremflix.app.ui.screens.splash

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import com.stremflix.app.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavHostController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isVideoReady = viewModel.isVideoReady.collectAsState().value
    
    // Placeholder for the actual video resource
    // In a real app, this would be R.raw.intro or a URL
    val rawVideoUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.intro)

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false // Hide UI controls
                    player = ExoPlayer.Builder(ctx).build().apply {
                        val mediaItem = MediaItem.fromUri(rawVideoUri)
                        setMediaItem(mediaItem)
                        prepare()
                        
                        // Requirement: Video starts paused on the first frame
                        playWhenReady = false
                        
                        addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(state: Int) {
                                if (state == Player.STATE_READY && !viewModel.isVideoReady.value) {
                                    viewModel.markVideoReady()
                                }
                                if (state == Player.STATE_ENDED) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            }
                        })
                    }
                    showNextBufferingAd = false
                    keepScreenOn = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    LaunchedEffect(isVideoReady) {
        if (isVideoReady) {
            // Requirement: Resume playback automatically and unmuted
            delay(500) // Small delay to ensure frame is visible
            viewModel.player?.play()
            viewModel.player?.volume = 1.0f
            
            // Fallback navigation if video doesn't end or loops
            delay(5000) 
            if (!navController.currentDestination?.route?.contains("home")!!) {
                 navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
        }
    }
}