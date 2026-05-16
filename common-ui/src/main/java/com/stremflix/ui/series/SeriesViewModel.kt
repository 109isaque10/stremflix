package com.stremflix.ui.series

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremflix.core.domain.model.ContentType
import com.stremflix.core.domain.model.Result
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.repository.ContentRepository
import com.stremflix.data.repository.TraktRepository
import com.stremflix.ui.R
import com.stremflix.ui.home.ContentRow
import com.stremflix.ui.home.SeriesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

            try {
                val rows = mutableListOf<ContentRow>()

                coroutineScope {
                    val trendingDef = async { contentRepository.getTrending(ContentType.SERIES, "week") }
                    val popularDef = async { contentRepository.getPopular(ContentType.SERIES) }
                    val topRatedDef = async { contentRepository.getTopRated(ContentType.SERIES) }
                    val airingDef = async { contentRepository.getCurrentlyAiring() }

                    val isTraktEnabled = checkTraktEnabled()
                    val upcomingDef = async {
                        if (isTraktEnabled) traktRepository.getUpcomingFromCalendar()
                        else contentRepository.getUpcomingSeries()
                    }
                    val anticipatedDef = async {
                        if (isTraktEnabled) traktRepository.getMostAnticipatedShows()
                        else contentRepository.getAnticipatedSeries()
                    }

                    val trending = trendingDef.await()
                    if (trending is Result.Success && trending.data.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_trending_series), trending.data))

                    val popular = popularDef.await()
                    if (popular is Result.Success && popular.data.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_popular_series), popular.data))

                    val topRated = topRatedDef.await()
                    if (topRated is Result.Success && topRated.data.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_top_rated_tv), topRated.data))

                    val airing = airingDef.await()
                    if (airing is Result.Success && airing.data.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_currently_airing), airing.data))

                    val upcoming = upcomingDef.await()
                    if (upcoming is Result.Success && upcoming.data.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_upcoming_series), upcoming.data))

                    val anticipated = anticipatedDef.await()
                    if (anticipated is Result.Success && anticipated.data.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_most_anticipated_shows), anticipated.data))
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