package com.stremflix.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SplashViewModel @Inject constructor() : ViewModel() {
    private val _isVideoReady = MutableStateFlow(false)
    val isVideoReady: StateFlow<Boolean> = _isVideoReady
    var player: ExoPlayer? = null

    fun markVideoReady() {
        _isVideoReady.value = true
    }
}