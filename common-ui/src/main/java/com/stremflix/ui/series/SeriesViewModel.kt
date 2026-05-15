package com.stremflix.ui.series

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremflix.core.domain.model.ContentType
import com.stremflix.core.domain.model.Result
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.model.ContentItem
import com.stremflix.data.repository.ContentRepository
import com.stremflix.data.repository.TraktRepository
import com.stremflix.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeriesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentRepository: ContentRepository,
    private val traktRepository: TraktRepository,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _uiState = MutableStateFlow<SeriesUiState>(SeriesUiState.Loading)
    val uiState: StateFlow<SeriesUiState> = _uiState.asStateFlow()

    init {
        loadSeriesContent()
    }

    fun loadSeriesContent() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = SeriesUiState.Loading

            val rows = mutableListOf<ContentRow>()

            try {
                // 1. Trending Series (TMDB - Week)
                val trending = contentRepository.getTrending(ContentType.SERIES, "week")
                if (trending is Result.Success && trending.data.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_trending_series),
                        items = trending.data
                    ))
                }

                // 2. Popular Series (TMDB)
                val popular = contentRepository.getPopular(ContentType.SERIES)
                if (popular is Result.Success && popular.data.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_popular_series),
                        items = popular.data
                    ))
                }

                // 3. Top Rated TV Shows (TMDB)
                val topRated = contentRepository.getTopRated(ContentType.SERIES)
                if (topRated is Result.Success && topRated.data.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_top_rated_tv),
                        items = topRated.data
                    ))
                }

                // 4. Currently Airing (TMDB)
                val currentlyAiring = contentRepository.getCurrentlyAiring()
                if (currentlyAiring is Result.Success && currentlyAiring.data.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_currently_airing),
                        items = currentlyAiring.data
                    ))
                }

                // 5. Upcoming (Trakt calendar or TMDB fallback)
                val isTraktEnabled = checkTraktEnabled()
                val upcoming = if (isTraktEnabled) {
                    traktRepository.getUpcomingFromCalendar()
                } else {
                    contentRepository.getUpcomingSeries()
                }
                if (upcoming is Result.Success && upcoming.data.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_upcoming_series),
                        items = upcoming.data
                    ))
                }

                // 6. Most Anticipated (Trakt or TMDB fallback)
                val anticipated = if (isTraktEnabled) {
                    traktRepository.getMostAnticipatedShows()
                } else {
                    contentRepository.getAnticipatedSeries()
                }
                if (anticipated is Result.Success && anticipated.data.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_most_anticipated_shows),
                        items = anticipated.data
                    ))
                }

                _uiState.value = SeriesUiState.Success(rows)

            } catch (e: Exception) {
                _uiState.value = SeriesUiState.Error(e.message ?: "Failed to load series")
            }
        }
    }

    private suspend fun checkTraktEnabled(): Boolean {
        return false // TODO: Implement
    }
}