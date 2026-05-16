// common-ui/src/main/java/com/stremflix/ui/home/HomeViewModel.kt

package com.stremflix.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremflix.core.domain.model.ContentType
import com.stremflix.core.domain.model.Result
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.model.ContentItem
import com.stremflix.data.repository.*
import com.stremflix.ui.R
import com.stremflix.ui.util.checkTraktEnabled
import com.stremflix.ui.util.handlePlayLogic
import com.stremflix.ui.util.populateImages
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContentRow(
    val title: String,
    val items: List<ContentItem>,
    val isLoading: Boolean = false,
    val isLarge: Boolean = false,
    val error: String? = null
)

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val rows: List<ContentRow>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

sealed class MoviesUiState {
    object Loading : MoviesUiState()
    data class Success(val rows: List<ContentRow>) : MoviesUiState()
    data class Error(val message: String) : MoviesUiState()
}

sealed class SeriesUiState {
    object Loading : SeriesUiState()
    data class Success(val rows: List<ContentRow>) : SeriesUiState()
    data class Error(val message: String) : SeriesUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentRepository: ContentRepository,
    private val myListRepository: MyListRepository,
    private val traktRepository: TraktRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val streamRepository: StreamRepository,
    private val preferencesDataSource: PreferencesDataSource,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _showStreamDialog = MutableStateFlow(false)
    val showStreamDialog = _showStreamDialog.asStateFlow()

    private val _streams = MutableStateFlow<List<com.stremflix.data.model.Stream>>(emptyList())
    val streams = _streams.asStateFlow()

    var currentSelectedItem: ContentItem? = null
        private set

    init {
        loadHomeContent()
    }

    fun onPlayClicked(item: ContentItem) {
        currentSelectedItem = item
        viewModelScope.launch(dispatchers.io) {
            handlePlayLogic(
                item = item,
                contentRepository = contentRepository,
                streamRepository = streamRepository,
                watchHistoryRepository = watchHistoryRepository,
                preferencesDataSource = preferencesDataSource,
                streamsFlow = _streams,
                showDialogFlow = _showStreamDialog
            )
        }
    }

    fun dismissStreamDialog() {
        _showStreamDialog.value = false
    }

    fun onStreamSelected(stream: com.stremflix.data.model.Stream?) {
        _showStreamDialog.value = false
    }

    fun loadHomeContent() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = HomeUiState.Loading

            val rows = mutableListOf<ContentRow>()
            val isTraktEnabled = checkTraktEnabled(preferencesDataSource)

            if (isTraktEnabled) {
                launch { // Run concurrently so it doesn't block the UI
                    try {
                        val historyResult = traktRepository.getWatchedHistory()
                        if (historyResult is Result.Success) {
                            watchHistoryRepository.syncHistoryFromTrakt(historyResult.data)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace() // Ignore network failures on background sync
                    }
                }
            }

            try {
                val continueWatching = loadContinueWatching()
                if (continueWatching.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_continue_watching), continueWatching))

                val becauseYouWatched = loadBecauseYouWatched()
                if (becauseYouWatched.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_because_you_watched), becauseYouWatched))

                val traktRecommendations = if (isTraktEnabled) loadTraktRecommendations() else emptyList()
                if (traktRecommendations.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_recommended_for_you), traktRecommendations))

                val trending = if (isTraktEnabled) loadTraktTrending() else loadTmdbTrendingToday()
                if (trending.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_trending_now), trending))

                val mostWatched = if (isTraktEnabled) loadTraktMostWatchedWeekly() else loadTmdbTrendingWeek()
                if (mostWatched.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_most_watched_week), mostWatched))

                _uiState.value = HomeUiState.Success(rows)

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to load content")
            }
        }
    }

    // Logic for Continue Watching
    private suspend fun loadContinueWatching(): List<ContentItem> {
        // Get domain models from the repository
        val history = watchHistoryRepository.getAllWatchHistory().first()

        // Group by seriesId to get the latest entry for each show/movie
        val latestEntries = history.distinctBy { it.seriesId }.take(10)

        return latestEntries.mapNotNull { entry ->
            val isMovie = entry.seasonNumber == 0

            val targetEpisode = if (entry.isCompleted && !isMovie) {
                // If completed, try to find the next episode metadata
                val nextEpResult = contentRepository.getSeasonEpisodes(entry.seriesId, entry.seasonNumber)
                val nextEp = (nextEpResult as? Result.Success)?.data?.find {
                    it.episodeNumber == entry.episodeNumber + 1
                }

                // If no next episode in current season, try season + 1
                nextEp ?: (contentRepository.getSeasonEpisodes(entry.seriesId, entry.seasonNumber + 1) as? Result.Success)
                    ?.data?.firstOrNull()
            } else {
                null
            }

            // Fetch the ContentItem details for the UI
            val details = contentRepository.getDetails(
                entry.seriesId,
                if (isMovie) ContentType.MOVIE else ContentType.SERIES
            )

            (details as? Result.Success)?.data?.let { item ->
                if (targetEpisode != null) {
                    // Return the item with "Next Episode" info if the previous was finished
                    item.copy(
                        title = "${item.title} - S${targetEpisode.seasonNumber}E${targetEpisode.episodeNumber}",
                        watchProgress = 0f
                    )
                } else {
                    // Return with resume progress
                    item.copy(watchProgress = entry.watchProgress)
                }
            }
        }
    }

    // Logic for Because You Watched
    private suspend fun loadBecauseYouWatched(): List<ContentItem> {
        val lastFinished = watchHistoryRepository.getAllWatchHistory().first()
            .firstOrNull { it.isCompleted } ?: return emptyList()

        val recommendations = if (lastFinished.seasonNumber == 0) {
            contentRepository.getRecommendations(lastFinished.seriesId, ContentType.MOVIE).dataOrNull() ?: emptyList()
        } else {
            contentRepository.getRecommendations(lastFinished.seriesId, ContentType.SERIES).dataOrNull() ?: emptyList()
        }

        return populateImages(recommendations, contentRepository)
    }

    private suspend fun loadTraktRecommendations(): List<ContentItem> {
        val result = traktRepository.getRecommendations().dataOrNull() ?: emptyList()
        return populateImages(result, contentRepository)
    }

    private suspend fun loadTraktTrending(): List<ContentItem> {
        val result = traktRepository.getTrending().dataOrNull() ?: emptyList()
        return populateImages(result, contentRepository)
    }

    private suspend fun loadTmdbTrendingToday(): List<ContentItem> {
        val movies = contentRepository.getTrending(ContentType.MOVIE, "day").dataOrNull() ?: emptyList()
        val series = contentRepository.getTrending(ContentType.SERIES, "day").dataOrNull() ?: emptyList()
        return populateImages(movies, contentRepository) + populateImages(series, contentRepository)
    }

    private suspend fun loadTmdbTrendingWeek(): List<ContentItem> {
        val movies = contentRepository.getTrending(ContentType.MOVIE, "week").dataOrNull() ?: emptyList()
        val series = contentRepository.getTrending(ContentType.SERIES, "week").dataOrNull() ?: emptyList()
        return populateImages(movies, contentRepository) + populateImages(series, contentRepository)
    }

    private suspend fun loadTraktMostWatchedWeekly(): List<ContentItem> {
        val result = traktRepository.getMostWatchedWeekly().dataOrNull() ?: emptyList()
        return populateImages(result, contentRepository)
    }
}