// data/src/main/java/com/stremflix/data/repository/TraktRepository.kt

package com.stremflix.data.repository

import com.stremflix.core.domain.model.Result
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.manager.TraktOAuthManager
import com.stremflix.data.mapper.toDomainItem
import com.stremflix.data.model.WatchHistory
import com.stremflix.data.remote.TraktApi
import com.stremflix.data.remote.dto.trakt.TraktScrobbleItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.mapNotNull

@Singleton
class TraktRepository @Inject constructor(
    private val traktApi: TraktApi,
    private val traktOAuthManager: TraktOAuthManager,
    private val preferencesDataSource: PreferencesDataSource,
    private val dispatchers: AppDispatchers
) {
    private suspend fun isTraktEnabled(): Boolean {
        val prefs = preferencesDataSource.preferencesFlow.first()
        val token = traktOAuthManager.getAccessTokenForRequest()
        return !prefs.traktClientId.isNullOrBlank() && token != null
    }

    // ============ WATCHLIST ============

    suspend fun getWatchlist(): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            if (!isTraktEnabled()) return@withContext Result.Success(emptyList())

            try {
                val watchlist = traktApi.getWatchlist()
                val items = watchlist.mapNotNull { item ->
                    when (item.type) {
                        "movie" -> item.movie?.toDomainItem()
                        "show" -> item.show?.toDomainItem()
                        else -> null
                    }
                }
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    suspend fun addToWatchlist(contentId: String, type: String): Result<out Unit> {
        return withContext(dispatchers.io) {
            if (!isTraktEnabled()) return@withContext Result.Success(Unit)

            try {
                when (type) {
                    "movie" -> traktApi.addToWatchlistMovie(contentId.toInt())
                    "show" -> traktApi.addToWatchlistShow(contentId.toInt())
                }
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    // ============ RECOMMENDATIONS ============

    suspend fun getRecommendations(limit: Int = 20): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            if (!isTraktEnabled()) return@withContext Result.Success(emptyList())

            try {
                val movies = traktApi.getMovieRecommendations(limit / 2)
                val shows = traktApi.getShowRecommendations(limit / 2)

                val movieItems = movies.mapNotNull { it.toDomainItem() }
                val showItems = shows.mapNotNull { it.toDomainItem() }

                Result.Success(movieItems + showItems)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    // ============ TRENDING ============

    suspend fun getTrending(limit: Int = 20): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            if (!isTraktEnabled()) return@withContext Result.Success(emptyList())

            try {
                val movies = traktApi.getTrendingMovies(limit / 2)
                val shows = traktApi.getTrendingShows(limit / 2)

                val movieItems = movies.mapNotNull { it.movie?.toDomainItem() }
                val showItems = shows.mapNotNull { it.show?.toDomainItem() }

                Result.Success(movieItems + showItems)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    // ============ MOST WATCHED ============

    suspend fun getMostWatchedWeekly(limit: Int = 20): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            if (!isTraktEnabled()) return@withContext Result.Success(emptyList())

            try {
                val movies = traktApi.getMostWatchedMovies("weekly", limit / 2)
                val shows = traktApi.getMostWatchedShows("weekly", limit / 2)

                val movieItems = movies.mapNotNull { it.movie?.toDomainItem() }
                val showItems = shows.mapNotNull { it.show?.toDomainItem() }

                Result.Success(movieItems + showItems)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    // ============ MOST ANTICIPATED ============

    suspend fun getMostAnticipatedMovies(limit: Int = 20): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            if (!isTraktEnabled()) return@withContext Result.Success(emptyList())

            try {
                val anticipated = traktApi.getMostAnticipatedMovies(limit)
                val items = anticipated.mapNotNull { it.movie?.toDomainItem() }
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    suspend fun getMostAnticipatedShows(limit: Int = 20): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            if (!isTraktEnabled()) return@withContext Result.Success(emptyList())

            try {
                val anticipated = traktApi.getMostAnticipatedShows(limit)
                val items = anticipated.mapNotNull { it.show?.toDomainItem() }
                Result.Success(items)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    // ============ CALENDAR ============

    suspend fun getUpcomingFromCalendar(limit: Int = 30): Result<out List<com.stremflix.data.model.ContentItem>> {
        return withContext(dispatchers.io) {
            if (!isTraktEnabled()) return@withContext Result.Success(emptyList())

            try {
                val calendar = traktApi.getCalendar(limit)
                val items = calendar.mapNotNull { it.show?.toDomainItem() }
                Result.Success(items.distinctBy { it.id })
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    // ============ WATCHED HISTORY ============

    suspend fun getWatchedHistory(): Result<out List<WatchHistory>> {
        return withContext(dispatchers.io) {
            if (!isTraktEnabled()) return@withContext Result.Success(emptyList())

            try {
                val movies = traktApi.getWatchedMovies()
                val shows = traktApi.getWatchedShows()

                // Convert to domain WatchHistory
                val history = mutableListOf<WatchHistory>()

                movies.forEach { watched ->
                    watched.movie?.let { movie ->
                        history.add(
                            WatchHistory(
                                id = movie.ids?.tmdb?.toString() ?: "",
                                type = com.stremflix.core.domain.model.ContentType.MOVIE,
                                title = movie.title ?: "",
                                watchedAt = null,
                                progress = 1.0f
                            )
                        )
                    }
                }

                shows.forEach { watchedShow ->
                    watchedShow.show?.let { show ->
                        watchedShow.seasons?.forEach { season ->
                            season.episodes?.forEach { episode ->
                                history.add(
                                    WatchHistory(  // FIX: Correct constructor params
                                        episodeId = "${show.ids?.trakt}_${season.number}_${episode.number}",
                                        seriesId = show.ids?.trakt?.toString() ?: "",
                                        seasonNumber = season.number,
                                        episodeNumber = episode.number,
                                        watched = true,
                                        watchProgress = 1.0f,
                                        lastWatchedAt = episode.last_watched_at?.toEpochMilliseconds(),
                                        title = show.title ?: "",
                                        synopsis = show.overview
                                    )
                                )
                            }
                        }
                    }
                }

                Result.Success(history)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    // ============ SCROBBLING ============

    suspend fun scrobbleStart(item: TraktScrobbleItem): Result<out Unit> {
        return withContext(dispatchers.io) {
            if (!isTraktEnabled()) return@withContext Result.Success(Unit)

            try {
                traktApi.scrobbleStart(item)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    suspend fun scrobblePause(item: TraktScrobbleItem): Result<out Unit> {
        return withContext(dispatchers.io) {
            if (!isTraktEnabled()) return@withContext Result.Success(Unit)

            try {
                traktApi.scrobblePause(item)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }

    suspend fun scrobbleStop(item: TraktScrobbleItem): Result<out Unit> {
        return withContext(dispatchers.io) {
            if (!isTraktEnabled()) return@withContext Result.Success(Unit)

            try {
                traktApi.scrobbleStop(item)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
            }
        }
    }
}