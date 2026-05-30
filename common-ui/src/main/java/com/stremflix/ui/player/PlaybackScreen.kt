package com.stremflix.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import com.stremflix.core.domain.model.ContentType
import com.stremflix.ui.theme.NetflixBlack

@OptIn(UnstableApi::class)
@Composable
fun PlaybackScreen(
    streamUrl: String,
    contentTitle: String,
    contentSynopsis: String?,
    contentId: String,
    contentType: String,
    isTvMode: Boolean,
    season: Int? = null,
    episode: Int? = null,
    navController: NavHostController,
    viewModel: PlaybackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showUpNext by viewModel.showUpNextModal.collectAsState()
    val upNextInfo by viewModel.upNextInfo.collectAsState()
    val countdown by viewModel.countdownSeconds.collectAsState()
    val progress by viewModel.currentPosition.collectAsState()
    val advisories by viewModel.advisories.collectAsState()
    val contentRating by viewModel.contentRating.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // ✅ Cleanup on exit
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(view, activity) {
        val window = activity?.window
        val insetsController = window?.let { WindowInsetsControllerCompat(it, view) }

        if (window != null && insetsController != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }

            // Hide system bars and allow swiping to temporarily show them
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())

            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        onDispose {
            if (window != null && insetsController != null) {
                // Restore system bars and orientation on exit
                WindowCompat.setDecorFitsSystemWindows(window, true)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    LaunchedEffect(streamUrl) {
        viewModel.initializePlayback(
            streamUrl = streamUrl,
            contentId = contentId,
            contentType = if (contentType == "movie") ContentType.MOVIE else ContentType.SERIES,
            season = season,
            episode = episode
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixBlack),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    this.player = viewModel.player
                    useController = false
                    setBackgroundColor(android.graphics.Color.BLACK)
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    keepScreenOn = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        val duration by viewModel.duration.collectAsState()
        val progressPercent = if (duration > 0) progress.toFloat() / duration.toFloat() else 0f

        CustomPlayerControls(
            player = viewModel.player,
            contentTitle = contentTitle,
            contentSynopsis = contentSynopsis,
            viewModel = viewModel,
            season = season,
            episode = episode,
            uiState = uiState,
            isTvMode = isTvMode,
            progress = progressPercent,
            onPlayPause = viewModel::onPlayPause,
            onSeek = viewModel::onSeekTo,
            navController = navController,
            modifier = Modifier.align(Alignment.Center)
        )

        ContentRatingPopup(
            rating = contentRating,
            advisories = advisories,
            modifier = Modifier.align(Alignment.TopStart)
        )

        if (showUpNext) {
            UpNextModal(
                info = upNextInfo,
                countdownSeconds = countdown,
                onPlayNow = viewModel::playNext,
                onDismiss = viewModel::onUpNextDismiss,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}