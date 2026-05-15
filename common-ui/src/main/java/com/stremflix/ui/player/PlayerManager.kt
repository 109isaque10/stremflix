package com.stremflix.ui.player

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var _player: ExoPlayer? = null
    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    val playbackState: StateFlow<Int> = _playbackState.asStateFlow()

    fun getPlayer(): ExoPlayer {
        // Ensure we're on main thread (Compose always is)
        if (_player == null) {
            _player = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        _playbackState.value = state
                    }
                })
            }
        }
        return _player!!
    }

    suspend fun setMediaItem(uri: String) = withContext(Dispatchers.Main) {
        val player = getPlayer()
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    suspend fun setMediaItemRes(resId: Int) = withContext(Dispatchers.Main) {
        val uri = Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).path(resId.toString()).build().toString()
        setMediaItem(uri)
    }

    suspend fun play() = withContext(Dispatchers.Main) {
        getPlayer().play()
    }

    suspend fun pause() = withContext(Dispatchers.Main) {
        getPlayer().pause()
    }

    suspend fun seekTo(position: Long) = withContext(Dispatchers.Main) {
        getPlayer().seekTo(position)
    }

    suspend fun release() = withContext(Dispatchers.Main) {
        _player?.release()
        _player = null
    }
}