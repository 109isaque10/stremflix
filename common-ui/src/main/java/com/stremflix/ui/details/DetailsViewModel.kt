package com.stremflix.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.stremflix.core.domain.model.ContentType
import com.stremflix.core.domain.model.Result
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.model.ContentItem
import com.stremflix.data.model.Episode
import com.stremflix.data.model.Stream
import com.stremflix.data.repository.ContentRepository
import com.stremflix.data.repository.StreamRepository
import com.stremflix.data.repository.WatchHistoryRepository
import com.stremflix.ui.navigation.AppRoute
import com.stremflix.ui.util.determineUpNext
import com.stremflix.ui.util.handlePlayLogic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailsUiState {
    object Loading : DetailsUiState()
    data class Success(val item: ContentItem) : DetailsUiState()
    data class Error(val message: String) : DetailsUiState()
}

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val streamRepository: StreamRepository,
    private val preferencesDataSource: PreferencesDataSource,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val args: AppRoute.Details = savedStateHandle.toRoute<AppRoute.Details>()
    val contentId = args.id
    val contentTitle = args.contentTitle
    val contentSynopsis = args.contentSynopsis
    private val contentTypeStr = args.type
    val contentType = if (contentTypeStr == "movie") ContentType.MOVIE else ContentType.SERIES

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private val _streams = MutableStateFlow<List<Stream>>(emptyList())
    val streams: StateFlow<List<Stream>> = _streams.asStateFlow()

    private val _showStreamDialog = MutableStateFlow(false)
    val showStreamDialog: StateFlow<Boolean> = _showStreamDialog.asStateFlow()

    var currentSelectedItem: ContentItem? = null
        private set
    private val _seasons = MutableStateFlow<List<Int>>(emptyList())
    var seasons: StateFlow<List<Int>> = _seasons.asStateFlow()
    private val _currentSeason = MutableStateFlow(1)
    var currentSeason: StateFlow<Int> = _currentSeason.asStateFlow()
    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    var episodes: StateFlow<List<Episode>> = _episodes.asStateFlow()

    private val _selectedEpisode = MutableStateFlow<Episode?>(null)
    var selectedEpisode: StateFlow<Episode?> = _selectedEpisode.asStateFlow()

    private val _playFromBeggining = MutableStateFlow(false)
    var playFromBeggining: StateFlow<Boolean> = _playFromBeggining.asStateFlow()

    init {
        loadDetails()
    }

    private fun loadDetails() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = DetailsUiState.Loading

            // Fetch details
            val result = contentRepository.getDetails(contentId, contentType)

            if (result is Result.Success) {
                currentSelectedItem = result.data

                // If it's a TV show, load seasons and episodes
                if (contentType == ContentType.SERIES) {
                    result.data.numberOfSeasons?.let { numSeasons ->
                        _seasons.value = (1..numSeasons).toList()
                    } ?: run {
                        _seasons.value = (1..10).toList() // Fallback
                    }

                    _selectedEpisode.value = determineUpNext(result.data, watchHistoryRepository, contentRepository)
                    val seasonToLoad = _selectedEpisode.value?.seasonNumber ?: 1
                    loadEpisodesForSeason(result.data.id, seasonToLoad)
                }

                _uiState.value = DetailsUiState.Success(result.data)
            } else {
                _uiState.update { DetailsUiState.Error("Failed to load details") }
            }
        }
    }

    fun onPlayClicked(episode: Episode?) {
        viewModelScope.launch(dispatchers.io) {
            val item = currentSelectedItem ?: return@launch
            handlePlayLogic(
                item = item,
                specificEpisode = episode,
                playFromBeggining = playFromBeggining.value,
                contentRepository = contentRepository,
                streamRepository = streamRepository,
                watchHistoryRepository = watchHistoryRepository,
                preferencesDataSource = preferencesDataSource,
                streamsFlow = _streams,
                showDialogFlow = _showStreamDialog
            )
        }
    }

    fun onStreamSelected(stream: Stream?) {
        _showStreamDialog.value = false
        // Don't navigate here - let the UI handle it via callback
    }

    private suspend fun loadEpisodesForSeason(
        seriesId: String,
        seasonNumber: Int,
        updateSelectedEpisode: Boolean = false
    ) {
        val result = contentRepository.getSeasonEpisodes(seriesId, seasonNumber)
        if (result is Result.Success) {
            _episodes.value = result.data
            _currentSeason.value = seasonNumber
            if (updateSelectedEpisode) {
                _selectedEpisode.value = result.data.firstOrNull()
            }
        } else {
            // Show error state if needed
            _episodes.value = emptyList()
        }
    }

    fun onSeasonSelected(seasonNumber: Int) {
        viewModelScope.launch(dispatchers.io) {
            currentSelectedItem?.let { item ->
                _episodes.value = emptyList()
                loadEpisodesForSeason(item.id, seasonNumber, updateSelectedEpisode = true)
            }
        }
    }
}