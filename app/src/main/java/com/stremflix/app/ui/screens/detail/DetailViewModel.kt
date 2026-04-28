package com.stremflix.app.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremflix.core.model.Episode
import com.stremflix.core.model.MediaItem
import com.stremflix.core.model.MediaType
import com.stremflix.core.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val mediaItem: MediaItem? = null,
    val cast: List<String> = emptyList(),
    val episodes: List<Episode> = emptyList(),
    val similarItems: List<MediaItem> = emptyList(),
    val streamUrl: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getMediaDetailUseCase: GetMediaDetailUseCase,
    private val getCastUseCase: GetCastUseCase,
    private val getEpisodesUseCase: GetEpisodesUseCase,
    private val getSimilarUseCase: GetSimilarUseCase,
    private val getStreamUseCase: GetStreamUseCase,
    private val toggleMyListUseCase: ToggleMyListUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()
    
    fun loadDetail(mediaId: String, mediaType: MediaType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val media = getMediaDetailUseCase(mediaId, mediaType)
                val cast = getCastUseCase(mediaId, mediaType)
                val similar = getSimilarUseCase(mediaId, mediaType)
                val streamUrl = getStreamUseCase(mediaId, mediaType, null, null)
                
                _uiState.value = DetailUiState(
                    mediaItem = media,
                    cast = cast,
                    similarItems = similar,
                    streamUrl = streamUrl,
                    isLoading = false
                )
                
                if (mediaType == MediaType.SERIES) {
                    loadEpisodes(mediaId, 1)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun loadEpisodes(seriesId: String, seasonNumber: Int) {
        viewModelScope.launch {
            val episodes = getEpisodesUseCase(seriesId, seasonNumber)
            _uiState.value = _uiState.value.copy(episodes = episodes)
        }
    }
    
    fun getEpisodeStream(seriesId: String, season: Int, episode: Int): String? {
        return getStreamUseCase(seriesId, MediaType.SERIES, season, episode)
    }
    
    fun toggleMyList(mediaId: String) {
        viewModelScope.launch {
            toggleMyListUseCase(mediaId)
        }
    }
}