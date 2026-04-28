package com.stremflix.app.ui.screens.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremflix.core.usecase.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val tmdbApiKey: String = "",
    val traktApiKey: String = "",
    val stremioManifestUrl: String = "",
    val subtitleSize: TextUnit = 16.sp,
    val subtitleColor: Color = Color.White,
    val skipBackDuration: Int = 10,
    val muteOnStartup: Boolean = false,
    val trailerTimeout: Long = 5
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val saveSettingsUseCase: SaveSettingsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    fun updateTmdbKey(key: String) {
        _uiState.value = _uiState.value.copy(tmdbApiKey = key)
        saveSettingsUseCase(_uiState.value.copy(tmdbApiKey = key))
    }
    
    fun updateTraktKey(key: String) {
        _uiState.value = _uiState.value.copy(traktApiKey = key)
        saveSettingsUseCase(_uiState.value.copy(traktApiKey = key))
    }
    
    fun updateStremioUrl(url: String) {
        _uiState.value = _uiState.value.copy(stremioManifestUrl = url)
        saveSettingsUseCase(_uiState.value.copy(stremioManifestUrl = url))
    }
    
    fun updateSubtitleSize(size: Float) {
        _uiState.value = _uiState.value.copy(subtitleSize = size.sp)
        saveSettingsUseCase(_uiState.value.copy(subtitleSize = size.sp))
    }
    
    fun updateSkipDuration(duration: Int) {
        _uiState.value = _uiState.value.copy(skipBackDuration = duration)
        saveSettingsUseCase(_uiState.value.copy(skipBackDuration = duration))
    }
    
    fun toggleMuteOnStartup() {
        _uiState.value = _uiState.value.copy(muteOnStartup = !_uiState.value.muteOnStartup)
        saveSettingsUseCase(_uiState.value.copy(muteOnStartup = !_uiState.value.muteOnStartup))
    }
}