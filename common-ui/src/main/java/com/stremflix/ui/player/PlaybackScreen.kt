package com.stremflix.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
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
    season: Int? = null,
    episode: Int? = null,
    onBack: () -> Unit,
    viewModel: PlaybackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showUpNext by viewModel.showUpNextModal.collectAsState()
    val upNextInfo by viewModel.upNextInfo.collectAsState()
    val countdown by viewModel.countdownSeconds.collectAsState()
    val progress by viewModel.currentPosition.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity
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
                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    keepScreenOn = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        CustomPlayerControls(
            player = viewModel.player,
            contentTitle = contentTitle,
            contentSynopsis = contentSynopsis,
            viewModel = viewModel,
            season = season,
            episode = episode,
            uiState = uiState,
            progress = progress,
            onPlayPause = viewModel::onPlayPause,
            onSeek = viewModel::onSeekTo,
            onBack = onBack,
            modifier = Modifier.align(Alignment.Center)
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