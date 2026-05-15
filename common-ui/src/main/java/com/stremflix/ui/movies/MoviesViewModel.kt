// common-ui/src/main/java/com/stremflix/ui/movies/MoviesViewModel.kt

package com.stremflix.ui.movies

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
class MoviesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentRepository: ContentRepository,
    private val traktRepository: TraktRepository,
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

            val rows = mutableListOf<ContentRow>()

            try {
                // 1. Trending Movies (TMDB - This Week)
                val trending = contentRepository.getTrending(ContentType.MOVIE, "week")
                if (trending is Result.Success && trending.data.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_trending_movies),
                        items = trending.data
                    ))
                }

                // 2. Popular Movies (TMDB)
                val popular = contentRepository.getPopular(ContentType.MOVIE)
                if (popular is Result.Success && popular.data.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_popular_movies),
                        items = popular.data
                    ))
                }

                // 3. Top Rated Movies (TMDB)
                val topRated = contentRepository.getTopRated(ContentType.MOVIE)
                if (topRated is Result.Success && topRated.data.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_top_rated_movies),
                        items = topRated.data
                    ))
                }

                // 4. Now Playing (TMDB)
                val nowPlaying = contentRepository.getNowPlaying()
                if (nowPlaying is Result.Success && nowPlaying.data.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_now_playing),
                        items = nowPlaying.data
                    ))
                }

                // 5. Most Anticipated (Trakt or TMDB fallback)
                val isTraktEnabled = checkTraktEnabled()
                val anticipated = if (isTraktEnabled) {
                    traktRepository.getMostAnticipatedMovies()
                } else {
                    contentRepository.getUpcomingMovies()
                }
                if (anticipated is Result.Success && anticipated.data.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_most_anticipated_movies),
                        items = anticipated.data
                    ))
                }

                // 6. Browse by Genre (TMDB Discover)
                val byGenre = contentRepository.getMoviesByGenre()
                if (byGenre is Result.Success && byGenre.data.isNotEmpty()) {
                    rows.add(ContentRow(
                        title = context.getString(R.string.row_browse_by_genre),
                        items = byGenre.data
                    ))
                }

                _uiState.value = MoviesUiState.Success(rows)

            } catch (e: Exception) {
                _uiState.value = MoviesUiState.Error(e.message ?: "Failed to load movies")
            }
        }
    }

    private suspend fun checkTraktEnabled(): Boolean {
        // Check preferences
        return false
    }
}