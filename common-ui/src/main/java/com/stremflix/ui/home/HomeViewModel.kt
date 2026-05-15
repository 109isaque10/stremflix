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
import com.stremflix.data.repository.ContentRepository
import com.stremflix.data.repository.MyListRepository
import com.stremflix.data.repository.TraktRepository
import com.stremflix.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContentRow(
    val title: String,
    val items: List<ContentItem>,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val rows: List<ContentRow>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentRepository: ContentRepository,
    private val myListRepository: MyListRepository,
    private val traktRepository: TraktRepository,
    private val preferencesDataSource: PreferencesDataSource,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeContent()
    }

    fun loadHomeContent() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = HomeUiState.Loading

            val rows = mutableListOf<ContentRow>()

            try {
                // 1. Continue Watching (from local DB)
                val continueWatching = loadContinueWatching()
                if (continueWatching.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_continue_watching),
                        items = continueWatching
                    ))
                }

                // 2. Because You Watched [X] (TMDB recommendations)
                val becauseYouWatched = loadBecauseYouWatched()
                if (becauseYouWatched.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_because_you_watched),
                        items = becauseYouWatched
                    ))
                }

                // 3. Recommended for You (Trakt, if enabled)
                val isTraktEnabled = checkTraktEnabled()
                if (isTraktEnabled) {
                    val traktRecommendations = loadTraktRecommendations()
                    if (traktRecommendations.isNotEmpty()) {
                        rows.add(ContentRow(
                            title = context.getString(R.string.row_recommended_for_you),
                            items = traktRecommendations
                        ))
                    }
                }

                // 4. Trending Now (Trakt or TMDB fallback)
                val trending = if (isTraktEnabled) {
                    loadTraktTrending()
                } else {
                    loadTmdbTrendingToday()
                }
                if (trending.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_trending_now),
                        items = trending
                    ))
                }

                // 5. Most Watched This Week (Trakt or TMDB fallback)
                val mostWatched = if (isTraktEnabled) {
                    loadTraktMostWatchedWeekly()
                } else {
                    loadTmdbTrendingWeek()
                }
                if (mostWatched.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_most_watched_week),
                        items = mostWatched
                    ))
                }

                _uiState.value = HomeUiState.Success(rows)

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to load content")
            }
        }
    }

    private suspend fun loadContinueWatching(): List<ContentItem> {
        // Fetch from watch history DB
        return emptyList()
    }

    private suspend fun loadBecauseYouWatched(): List<ContentItem> {
        // Get most recently watched item
        // Fetch TMDB recommendations for that item
        return emptyList()
    }

    private suspend fun loadTraktRecommendations(): List<ContentItem> {
        val result = traktRepository.getRecommendations()
        return result.dataOrNull() ?: emptyList()
    }

    private suspend fun loadTraktTrending(): List<ContentItem> {
        val result = traktRepository.getTrending()
        return result.dataOrNull() ?: emptyList()
    }

    private suspend fun loadTmdbTrendingToday(): List<ContentItem> {
        val movies = contentRepository.getTrending(ContentType.MOVIE, "day")
        val series = contentRepository.getTrending(ContentType.SERIES, "day")
        return (movies.dataOrNull() ?: emptyList()) + (series.dataOrNull() ?: emptyList())
    }

    private suspend fun loadTmdbTrendingWeek(): List<ContentItem> {
        val movies = contentRepository.getTrending(ContentType.MOVIE, "week")
        val series = contentRepository.getTrending(ContentType.SERIES, "week")
        return (movies.dataOrNull() ?: emptyList()) + (series.dataOrNull() ?: emptyList())
    }

    private suspend fun loadTraktMostWatchedWeekly(): List<ContentItem> {
        val result = traktRepository.getMostWatchedWeekly()
        return result.dataOrNull() ?: emptyList()
    }

    private suspend fun checkTraktEnabled(): Boolean {
        val prefs = preferencesDataSource.preferencesFlow.first()
        return !prefs.traktClientId.isNullOrBlank()
    }
}