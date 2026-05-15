package com.stremflix.ui.player

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.stremflix.ui.theme.NetflixBlack

@Composable
fun ExoPlayerView(
    player: androidx.media3.common.Player,
    modifier: Modifier = Modifier,
    useController: Boolean = false
) {
    val context = LocalContext.current

    DisposableEffect(player) {
        val playerView = PlayerView(context).apply {
            this.player = player
            this.useController = useController
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(android.graphics.Color.parseColor("#141414"))
        }

        onDispose {
            playerView.player = null
        }
    }

    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
                this.useController = useController
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.parseColor("#141414"))
            }
        },
        modifier = modifier
    )
}