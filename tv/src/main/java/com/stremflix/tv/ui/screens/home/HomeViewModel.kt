package com.stremflix.tv.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremflix.core.model.MediaItem
import com.stremflix.core.usecase.GetHomeContentUseCase
import com.stremflix.core.usecase.ToggleMyListUseCase
import com.stremflix.core.usecase.GetSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TvHomeUiState(
    val heroItems: List<MediaItem>? = null,
    val rows: List<ContentRow> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeContentUseCase: GetHomeContentUseCase,
    private val toggleMyListUseCase: ToggleMyListUseCase,
    private val getSettingsUseCase: GetSettingsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TvHomeUiState())
    val uiState: StateFlow<TvHomeUiState> = _uiState.asStateFlow()
    
    init {
        loadContent()
    }
    
    private fun loadContent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val content = getHomeContentUseCase()
                _uiState.value = TvHomeUiState(
                    heroItems = content.take(5),
                    rows = listOf(
                        ContentRow("Popular on StreamFlix", content.take(10)),
                        ContentRow("Trending Now", content.drop(10).take(10)),
                        ContentRow("New Releases", content.drop(20).take(10))
                    ),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun toggleMyList(mediaId: String) {
        viewModelScope.launch {
            toggleMyListUseCase(mediaId)
        }
    }
    
    fun getTrailerTimeout(): Long {
        return getSettingsUseCase().trailerPreviewTimeout
    }
}