// data/src/main/java/com/stremflix/data/repository/TmdbRepository.kt

package com.stremflix.data.repository

import com.stremflix.core.domain.model.ContentType
import com.stremflix.core.domain.model.Result
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.mapper.toDomainItem
import com.stremflix.data.remote.TmdbApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbRepository @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val preferencesDataSource: PreferencesDataSource,
    private val dispatchers: AppDispatchers
) {
    private suspend fun getLanguage(): String {
        return preferencesDataSource.tmdbLanguage.first() ?: "en-US"
    }

    // ============ TRENDING ============

    suspend fun getTrendingMovies(timeWindow: String = "week"): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApi.getTrending("movie", timeWindow)
                val items = response.results?.mapNotNull { it.toDomainItem() } ?: emptyList()
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    suspend fun getTrendingSeries(timeWindow: String = "week"): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApi.getTrending("tv", timeWindow)
                val items = response.results?.mapNotNull { it.toDomainItem() } ?: emptyList()
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    // ============ POPULAR ============

    suspend fun getPopularMovies(page: Int = 1): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApi.getPopularMovies(page)
                val items = response.results?.mapNotNull { it.toDomainItem() } ?: emptyList()
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    suspend fun getPopularSeries(page: Int = 1): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApi.getPopularTv(page)
                val items = response.results?.mapNotNull { it.toDomainItem() } ?: emptyList()
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    // ============ TOP RATED ============

    suspend fun getTopRatedMovies(page: Int = 1): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApi.getTopRatedMovies(page)
                val items = response.results?.mapNotNull { it.toDomainItem() } ?: emptyList()
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    suspend fun getTopRatedSeries(page: Int = 1): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApi.getTopRatedTv(page)
                val items = response.results?.mapNotNull { it.toDomainItem() } ?: emptyList()
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    // ============ NOW PLAYING ============

    suspend fun getNowPlayingMovies(page: Int = 1): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApi.getNowPlayingMovies(page)
                val items = response.results?.mapNotNull { it.toDomainItem() } ?: emptyList()
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    // ============ UPCOMING ============

    suspend fun getUpcomingMovies(page: Int = 1): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApi.getUpcomingMovies(page)
                val items = response.results?.mapNotNull { it.toDomainItem() } ?: emptyList()
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    // ============ CURRENTLY AIRING ============

    suspend fun getCurrentlyAiringSeries(page: Int = 1): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApi.getCurrentlyAiringTv(page)
                val items = response.results?.mapNotNull { it.toDomainItem() } ?: emptyList()
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    // ============ DISCOVER ============

    suspend fun discoverMovies(
        genre: Int? = null,
        sortBy: String = "popularity.desc",
        page: Int = 1
    ): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApi.discoverMovies(
                    genre = genre,
                    page = page
                )
                val items = response.results?.mapNotNull { it.toDomainItem() } ?: emptyList()
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    suspend fun discoverSeries(
        genre: Int? = null,
        sortBy: String = "popularity.desc",
        page: Int = 1
    ): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApi.discoverTvShows(
                    genre = genre,
                    page = page
                )
                val items = response.results?.mapNotNull { it.toDomainItem() } ?: emptyList()
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    // ============ RECOMMENDATIONS ============

    suspend fun getMovieRecommendations(movieId: Int, page: Int = 1): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApi.getMovieRecommendations(movieId, page)
                val items = response.results?.mapNotNull { it.toDomainItem() } ?: emptyList()
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    suspend fun getSeriesRecommendations(seriesId: Int, page: Int = 1): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApi.getTvRecommendations(seriesId, page)
                val items = response.results?.mapNotNull { it.toDomainItem() } ?: emptyList()
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    // ============ GENRES ============

    suspend fun getMovieGenres(): Result<out List<com.stremflix.data.remote.dto.tmdb.GenreDto>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApi.getMovieGenres()
                Result.Success(response.genres)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    suspend fun getTvGenres(): Result<out List<com.stremflix.data.remote.dto.tmdb.GenreDto>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApi.getTvGenres()
                Result.Success(response.genres)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }
}