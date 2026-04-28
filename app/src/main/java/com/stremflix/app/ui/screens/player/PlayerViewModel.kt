package com.stremflix.app.ui.screens.player

import androidx.lifecycle.ViewModel
import com.stremflix.core.model.PlaybackSettings
import com.stremflix.core.usecase.GetPlaybackSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getPlaybackSettingsUseCase: GetPlaybackSettingsUseCase
) : ViewModel() {
    
    fun getPlaybackSettings(): PlaybackSettings {
        return getPlaybackSettingsUseCase()
    }
    
    fun saveProgress(mediaId: String, progressMs: Long) {
        // Save to watch progress
    }
}