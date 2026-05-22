package com.stremflix.ui.movies

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
import com.stremflix.ui.home.MoviesUiState
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
class MoviesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentRepository: ContentRepository,
    private val traktRepository: TraktRepository,
    private val preferencesDataSource: PreferencesDataSource,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _uiState = MutableStateFlow<MoviesUiState>(MoviesUiState.Loading)
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    init {
        loadMoviesContent()
    }

    fun loadMoviesContent() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = MoviesUiState.Loading

            val isTraktEnabled = checkTraktEnabled(preferencesDataSource)

            val rowDefinitions = listOf(
                context.getString(R.string.row_trending_movies) to suspend { contentRepository.getTrending(ContentType.MOVIE).dataOrNull() },
                context.getString(R.string.row_popular_movies) to suspend { contentRepository.getPopular(ContentType.MOVIE).dataOrNull() },
                context.getString(R.string.row_top_rated_movies) to suspend { contentRepository.getTopRated(ContentType.MOVIE).dataOrNull() },
                context.getString(R.string.row_now_playing) to suspend { contentRepository.getNowPlaying().dataOrNull() },
                context.getString(R.string.row_most_anticipated_movies) to suspend { val anticipated = if (isTraktEnabled) traktRepository.getMostAnticipatedMovies().dataOrNull() else contentRepository.getUpcomingMovies().dataOrNull(); populateImages(anticipated ?: emptyList(), contentRepository = contentRepository) }
            )

//            val byGenre = contentRepository.getMoviesByGenre()
//            if (byGenre is Result.Success && byGenre.data.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_browse_by_genre), byGenre.data))

            val results = contentRepository.loadInParallel<Pair<String, suspend () -> List<ContentItem>>, Pair<String, List<ContentItem>>>(
                rowDefinitions as List<Pair<String, suspend () -> List<ContentItem>>>,
                concurrencyLimit = 8
            ) { (title, loader) ->
                val items = try { loader() } catch (e: Exception) { emptyList<ContentItem>() } // Explicitly type emptyList
                title to items
            }

            val rows: List<ContentRow> = results.filter { it.second.isNotEmpty() }.map { ContentRow(it.first, it.second) }

            if (rows.isEmpty()) {
                _uiState.value = MoviesUiState.Error("No content available.")
            } else {
                _uiState.value = MoviesUiState.Success(rows)
            }
        }
    }
}