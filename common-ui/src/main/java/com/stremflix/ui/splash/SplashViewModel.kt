package com.stremflix.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremflix.core.domain.model.ContentType
import com.stremflix.core.domain.model.Result
import com.stremflix.core.util.AppDispatchers
import com.stremflix.core.util.ContentLoadManager
import com.stremflix.core.util.ContentLoadState
import com.stremflix.data.repository.ContentRepository
import com.stremflix.ui.R
import com.stremflix.ui.player.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class SplashState {
    object Preparing : SplashState()
    object WaitingForContent : SplashState()
    object LoadingData : SplashState()
    object ReadyToPlay : SplashState() // Data loaded, video resumes.
    object Finished : SplashState() // Video ended, transition to Home.
    data class Error(val message: String) : SplashState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    val playerManager: PlayerManager,
    private val contentRepository: ContentRepository,
    private val contentLoadManager: ContentLoadManager,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Preparing)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    private var _contentLoaded = false
    val contentLoaded: Boolean get() = _contentLoaded

    init {
        initializeSplash()
    }

    private fun initializeSplash() {
        viewModelScope.launch(dispatchers.main) {
            playerManager.setMediaItemRes(R.raw.splash_intro)
            playerManager.pause()
            playerManager.seekTo(0)
        }

        viewModelScope.launch(dispatchers.io) {
            contentLoadManager.state.collect { loadState ->
                when (loadState) {
                    is ContentLoadState.NotStarted -> {
                        // Trigger HomeViewModel to start loading (optional)
                        // Or just wait for it to start on its own
                    }
                    is ContentLoadState.Loading -> {
                        _state.value = SplashState.WaitingForContent
                    }
                    is ContentLoadState.Loaded -> {
                        _state.value = SplashState.ReadyToPlay // ✅ Content ready → play video
                    }
                    is ContentLoadState.Error -> {
                        // Option 1: Still play splash (graceful degradation)
                        _state.value = SplashState.ReadyToPlay
                        // Option 2: Show error
                        // _state.value = SplashState.Error(loadState.message)
                    }
                }
            }
        }
    }

    fun onVideoEnded() {
        _state.value = SplashState.Finished
    }

    fun startPlayback() {
        if (_state.value == SplashState.ReadyToPlay) {
            viewModelScope.launch(dispatchers.main) {
                playerManager.play()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Do not release player here if it's a singleton used elsewhere,
        // but for Splash specific player, we might release.
        // Assuming PlayerManager is singleton for the app, we don't release.
    }
}