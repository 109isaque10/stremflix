package com.stremflix.ui.movies

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremflix.core.domain.model.ContentType
import com.stremflix.core.domain.model.Result
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.repository.ContentRepository
import com.stremflix.data.repository.TraktRepository
import com.stremflix.ui.R
import com.stremflix.ui.home.ContentRow
import com.stremflix.ui.home.MoviesUiState
import com.stremflix.ui.util.checkTraktEnabled
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

            try {
                val rows = mutableListOf<ContentRow>()

                val isTraktEnabled = checkTraktEnabled(preferencesDataSource)

                val trending = contentRepository.getTrending(ContentType.MOVIE)
                if (trending is Result.Success && trending.data.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_trending_movies), trending.data))

                val popular = contentRepository.getPopular(ContentType.MOVIE)
                if (popular is Result.Success && popular.data.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_popular_movies), popular.data))

                val topRated = contentRepository.getTopRated(ContentType.MOVIE)
                if (topRated is Result.Success && topRated.data.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_top_rated_movies), topRated.data))

                val nowPlaying = contentRepository.getNowPlaying()
                if (nowPlaying is Result.Success && nowPlaying.data.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_now_playing), nowPlaying.data))

                val anticipated = if (isTraktEnabled) traktRepository.getMostAnticipatedMovies() else contentRepository.getUpcomingMovies()
                if (anticipated is Result.Success && anticipated.data.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_most_anticipated_movies), anticipated.data))

                val byGenre = contentRepository.getMoviesByGenre()
                if (byGenre is Result.Success && byGenre.data.isNotEmpty()) rows.add(ContentRow(context.getString(R.string.row_browse_by_genre), byGenre.data))

                _uiState.value = MoviesUiState.Success(rows)

            } catch (e: Exception) {
                _uiState.value = MoviesUiState.Error(e.message ?: "Failed to load movies")
            }
        }
    }
}