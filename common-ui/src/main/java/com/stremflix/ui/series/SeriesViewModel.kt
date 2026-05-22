package com.stremflix.ui.series

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremflix.core.domain.model.ContentType
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.model.ContentItem
import com.stremflix.data.repository.ContentRepository
import com.stremflix.data.repository.TraktRepository
import com.stremflix.ui.R
import com.stremflix.ui.home.ContentRow
import com.stremflix.ui.home.SeriesUiState
import com.stremflix.ui.util.checkTraktEnabled
import com.stremflix.ui.util.populateImages
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val preferencesDataSource: PreferencesDataSource,
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

            val isTraktEnabled = checkTraktEnabled(preferencesDataSource)

            val rowDefinitions = listOf(
                context.getString(R.string.row_trending_series) to suspend { contentRepository.getTrending(ContentType.SERIES).dataOrNull() },
                context.getString(R.string.row_popular_series) to suspend { contentRepository.getPopular(ContentType.SERIES).dataOrNull() },
                context.getString(R.string.row_top_rated_tv) to suspend { contentRepository.getTopRated(ContentType.SERIES).dataOrNull() },
                context.getString(R.string.row_currently_airing) to suspend { contentRepository.getCurrentlyAiring().dataOrNull() },
                context.getString(R.string.row_upcoming_series) to suspend { val anticipated = if (isTraktEnabled) traktRepository.getUpcomingFromCalendar().dataOrNull() else contentRepository.getUpcomingSeries().dataOrNull(); populateImages(anticipated ?: emptyList(), contentRepository = contentRepository) },
                context.getString(R.string.row_most_anticipated_shows) to suspend { val anticipated = if (isTraktEnabled) traktRepository.getMostAnticipatedShows().dataOrNull() else contentRepository.getAnticipatedSeries().dataOrNull(); populateImages(anticipated ?: emptyList(), contentRepository = contentRepository) }
            )

            val results = contentRepository.loadInParallel<Pair<String, suspend () -> List<ContentItem>>, Pair<String, List<ContentItem>>>(
                rowDefinitions as List<Pair<String, suspend () -> List<ContentItem>>>,
                concurrencyLimit = 8
            ) { (title, loader) ->
                val items = try { loader() } catch (e: Exception) { emptyList<ContentItem>() } // Explicitly type emptyList
                title to items
            }

            val rows: List<ContentRow> = results.filter { it.second.isNotEmpty() }.map { ContentRow(it.first, it.second) }

            if (rows.isEmpty()) {
                _uiState.value = SeriesUiState.Error("No content available.")
            } else {
                _uiState.value = SeriesUiState.Success(rows)
            }
        }
    }
}