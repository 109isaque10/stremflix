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
import com.stremflix.data.model.ExternalIds
import com.stremflix.data.model.Stream
import com.stremflix.data.repository.ContentRepository
import com.stremflix.data.repository.StreamRepository
import com.stremflix.data.repository.WatchHistoryRepository
import com.stremflix.ui.navigation.AppRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    private var currentItem: ContentItem? = null
    private val _seasons = MutableStateFlow<List<Int>>(emptyList())
    var seasons: StateFlow<List<Int>> = _seasons.asStateFlow()
    private val _currentSeason = MutableStateFlow(1)
    var currentSeason: StateFlow<Int> = _currentSeason.asStateFlow()
    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    var episodes: StateFlow<List<Episode>> = _episodes.asStateFlow()

    init {
        loadDetails()
    }

    private fun loadDetails() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = DetailsUiState.Loading

            // Fetch details
            val result = contentRepository.getDetails(contentId, contentType)

            if (result is Result.Success) {
                currentItem = result.data

                // If it's a TV show, load seasons and episodes
                if (contentType == ContentType.SERIES) {
                    result.data.numberOfSeasons?.let { numSeasons ->
                        _seasons.value = (1..numSeasons).toList()
                    } ?: run {
                        _seasons.value = (1..10).toList() // Fallback
                    }

                    // Load episodes for season 1 initially
                    loadEpisodesForSeason(result.data.id, 1)
                }

                _uiState.value = DetailsUiState.Success(result.data)
            } else {
                _uiState.update { DetailsUiState.Error("Failed to load details") }
            }
        }
    }

    fun loadStreams(episode: Episode? = null) {
        viewModelScope.launch(dispatchers.io) {
            val item = currentItem ?: return@launch

            if (item.type == ContentType.MOVIE) {
                // For movies, just fetch streams
                fetchStreamsAndShowDialog(item.id, "movie", item.externalIds)
            } else {
                // For TV shows, determine which episode to play
                val episodeToPlay = episode ?: determineEpisodeToPlay(item)

                if (episodeToPlay != null) {
                    // For TV shows, you might need to fetch streams per episode

                    // Save watch progress before playing
                    if (episodeToPlay.watchProgress > 0f) {
                        watchHistoryRepository.updateWatchProgress(
                            seriesId = item.id,
                            seasonNumber = episodeToPlay.seasonNumber,
                            episodeNumber = episodeToPlay.episodeNumber,
                            progress = episodeToPlay.watchProgress
                        )
                    }

                    fetchStreamsAndShowDialog(item.id, "series", item.externalIds)
                }
            }
        }
    }

    fun onPlayClicked(episode: Episode?) {
        loadStreams()
    }

    fun onStreamSelected(stream: Stream?) {
        _showStreamDialog.value = false
        // Don't navigate here - let the UI handle it via callback
    }

    private suspend fun loadAndReturnFirstEpisode(seriesId: String): Episode? {
        loadEpisodesForSeason(seriesId, 1)
        return _episodes.value.firstOrNull()
    }

    private suspend fun loadEpisodesForSeason(seriesId: String, seasonNumber: Int) {
        val result = contentRepository.getSeasonEpisodes(seriesId, seasonNumber)
        if (result is Result.Success) {
            _episodes.value = result.data
            _currentSeason.value = seasonNumber
        } else {
            // Show error state if needed
            _episodes.value = emptyList()
        }
    }

    /**
     * Determines which episode to play based on watch history.
     * Priority:
     * 1. Continue watching (in-progress episode)
     * 2. Next episode after last watched
     * 3. First episode of series
     */
    private suspend fun determineEpisodeToPlay(item: ContentItem): Episode? {
        // Get all watch history for this series
        val watchHistory = watchHistoryRepository.getWatchHistory(item.id)

        // Find the last watched episode (most recent)
        val lastWatched = watchHistoryRepository.getLastWatchedEpisode(item.id)

        return when {
            // Case 1: No watch history at all - play first episode of season 1
            lastWatched == null -> {
                _episodes.value.firstOrNull() ?: loadAndReturnFirstEpisode(item.id)
            }

            // Case 2: Last episode is in progress - continue it
            lastWatched.isInProgress -> {
                _episodes.value.find {
                    it.seasonNumber == lastWatched.seasonNumber &&
                            it.episodeNumber == lastWatched.episodeNumber
                }
            }

            // Case 3: Last episode was completed - play next episode
            lastWatched.isCompleted -> {
                // Try to find next episode in current season
                val nextInSeason = _episodes.value.find {
                    it.seasonNumber == lastWatched.seasonNumber &&
                            it.episodeNumber == lastWatched.episodeNumber + 1
                }

                if (nextInSeason != null) {
                    return nextInSeason
                }

                // If no next episode in current season, try next season
                val nextSeason = lastWatched.seasonNumber + 1
                if (nextSeason <= (item.numberOfSeasons ?: 10)) {
                    loadEpisodesForSeason(item.id, nextSeason)
                    return _episodes.value.firstOrNull()
                }

                // If at end of series, loop back to beginning
                loadEpisodesForSeason(item.id, 1)
                _episodes.value.firstOrNull()
            }

            else -> {
                _episodes.value.firstOrNull()
            }
        }
    }

    fun onSeasonSelected(seasonNumber: Int) {
        viewModelScope.launch(dispatchers.io) {
            currentItem?.let { item ->
                _episodes.value = emptyList()
                loadEpisodesForSeason(item.id, seasonNumber)
            }
        }
    }

    private suspend fun fetchStreamsAndShowDialog(
        contentId: String,
        contentType: String,
        externalIds: ExternalIds
    ) {
        _showStreamDialog.value = true // Show loading dialog first
        _streams.value = emptyList()

        val prefs = preferencesDataSource.preferencesFlow.first()
        val idType = prefs.defaultIdType

        val result = streamRepository.getStreams(
            contentId = contentId,
            contentType = contentType,
            idType = idType,
            externalIds = externalIds
        )

        if (result is Result.Success && result.data.isNotEmpty()) {
            _streams.value = result.data
        } else {
            _streams.value = emptyList()
        }
    }
}