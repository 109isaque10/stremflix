package com.stremflix.commonui.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView
import com.stremflix.core.model.PlaybackSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@UnstableApi
class StremFlixPlayer(
    private val context: Context,
    private val playbackSettings: PlaybackSettings
) {
    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    
    private var exoPlayer: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var subtitleView: SubtitleView? = null
    
    enum class PlayerState {
        IDLE, BUFFERING, READY, ENDED, ERROR
    }
    
    fun initialize(playerView: PlayerView, subtitleView: SubtitleView? = null) {
        this.playerView = playerView
        this.subtitleView = subtitleView
        
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    _playerState.value = when (state) {
                        Player.STATE_BUFFERING -> PlayerState.BUFFERING
                        Player.STATE_READY -> PlayerState.READY
                        Player.STATE_ENDED -> PlayerState.ENDED
                        Player.STATE_IDLE -> PlayerState.IDLE
                        else -> PlayerState.IDLE
                    }
                }
                
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    _playerState.value = PlayerState.ERROR
                }            })
            
            // Apply playback settings
            applyPlaybackSettings()
        }
        
        playerView.player = exoPlayer
    }
    
    private fun ExoPlayer.applyPlaybackSettings() {
        // Mute on startup if configured
        if (playbackSettings.muteOnStartup) {
            volume = 0f
        }
        
        // Configure subtitle styling
        subtitleView?.let { view ->
            view.setApplyEmbeddedStyles(true)
            view.setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION)
            // Additional subtitle styling from settings
        }
    }
    
    fun loadMedia(
        url: String,
        headers: Map<String, String> = emptyMap(),
        subtitleUri: String? = null
    ) {
        exoPlayer?.let { player ->
            val mediaItem = MediaItem.Builder()
                .setUri(url)
                .setSubtitleConfigurations(
                    subtitleUri?.let { uri ->
                        listOf(
                            MediaItem.SubtitleConfiguration.Builder(uri.toUri())
                                .setId("subtitles")
                                .setMimeType(C.MIME_TEXT_VTT)
                                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                                .build()
                        )
                    } ?: emptyList()
                )
                .build()
            
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }
        fun play() {
        exoPlayer?.play()
    }
    
    fun pause() {
        exoPlayer?.pause()
    }
    
    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }
    
    fun skipBack() {
        exoPlayer?.let { player ->
            val newPosition = (player.currentPosition - playbackSettings.skipBackDuration).coerceAtLeast(0)
            player.seekTo(newPosition)
        }
    }
    
    fun skipForward() {
        exoPlayer?.let { player ->
            val newPosition = (player.currentPosition + playbackSettings.skipBackDuration)
                .coerceAtMost(player.duration)
            player.seekTo(newPosition)
        }
    }
    
    fun setVolume(volume: Float) {
        exoPlayer?.volume = volume
    }
    
    fun toggleMute() {
        exoPlayer?.let { player ->
            player.volume = if (player.volume > 0) 0f else 1f
        }
    }
    
    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        playerView = null
        subtitleView = null
    }
    
    fun getCurrentPosition(): Long = exoPlayer?.currentPosition ?: 0L
    fun getDuration(): Long = exoPlayer?.duration ?: 0L
    fun isPlaying(): Boolean = exoPlayer?.isPlaying ?: false
}