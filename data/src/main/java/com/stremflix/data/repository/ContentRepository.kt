package com.stremflix.data.repository

import com.stremflix.core.domain.model.ApiError
import com.stremflix.core.domain.model.ContentType
import com.stremflix.core.domain.model.Result
import com.stremflix.core.util.CacheConfig
import com.stremflix.core.util.cached
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.local.dao.ContentDao
import com.stremflix.data.mapper.toDomainItem
import com.stremflix.data.mapper.toEntity
import com.stremflix.data.mapper.toEpisode
import com.stremflix.data.model.ContentItem
import com.stremflix.data.model.Episode
import com.stremflix.data.remote.TmdbApi
import io.ktor.client.plugins.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class ContentRepository @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val preferencesDataSource: PreferencesDataSource,
    private val contentDao: ContentDao
) {

    suspend fun <T, R> loadInParallel(
        items: List<T>,
        concurrencyLimit: Int = 15,
        action: suspend (T) -> R
    ): List<R> = coroutineScope {
        val semaphore = Semaphore(concurrencyLimit)
        items.mapIndexed { index, item ->
            async {
                semaphore.withPermit {
                    // Stagger the initial burst so they don't hit the API at the exact same millisecond
                    if (index < concurrencyLimit) {
                        delay(index * 50L)
                    }
                    action(item)
                }
            }
        }.awaitAll()
    }

    suspend fun populateContentImages(items: List<ContentItem>): List<ContentItem> {
        if (items.isEmpty()) return emptyList()
        return loadInParallel(items, concurrencyLimit = 4) { item ->
            if (item.posterUrl.isNullOrEmpty()) {
                (getDetails(item.id, item.type) as? Result.Success)?.data ?: item
            } else {
                item
            }
        }
    }

    suspend fun <T> retryWithBackoff(
        maxRetries: Int,
        initialDelay: Long = 1000L, // 1 second
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        var attempt = 0

        while (true) {
            try {
                return block()
            } catch (e: ResponseException) {
                // Check for HTTP 429
                if (e.response.status.value == 429 && attempt < maxRetries) {
                    attempt++
                    // Exponential backoff
                    delay(currentDelay.milliseconds)
                    currentDelay = (currentDelay * 2).coerceAtMost(10000L) // cap at 10s
                } else {
                    throw e // Propagate the exception if it's not a 429 or max retries reached
                }
            }
        }
    }

    suspend fun getTrending(contentType: ContentType, timeWindow: String = "week"): Result<List<ContentItem>> {
        return cached(
            key = "trending_${contentType.name}_$timeWindow",
            ttlHours = CacheConfig.TRENDING_TTL_HOURS,
            fetcher = {
                val typeStr = if (contentType == ContentType.MOVIE) "movie" else "tv"
                tmdbApi.getTrending(typeStr, timeWindow)
            }
        ).let { response ->
            val items = response.results?.mapNotNull { it.toDomainItem() } ?: emptyList()

            // Cache in DB
            val entities = items.map { it.toEntity() }
            contentDao.insertContentList(entities)

            Result.Success(items)
        }
    }

    suspend fun getPopular(contentType: ContentType, page: Int = 1): Result<List<ContentItem>> {
        return cached(
            key = "popular_${contentType.name}_$page",
            ttlHours = CacheConfig.TRENDING_TTL_HOURS,
            fetcher = {
                if (contentType == ContentType.MOVIE) tmdbApi.getPopularMovies(page)
                else tmdbApi.getPopularTv(page)
            }
        ).let { response ->
            val items = response.results?.mapNotNull { it.toDomainItem() } ?: emptyList()
            Result.Success(items)
        }
    }

    suspend fun getDetails(contentId: String, contentType: ContentType): Result<out ContentItem> {
        // Try cache first (Room)
        val cachedEntity = contentDao.getContentById(contentId)
        if (cachedEntity != null && !cachedEntity.imdbId.isNullOrEmpty() && !cachedEntity.posterUrl.isNullOrEmpty()) {
            return Result.Success(cachedEntity.toDomainItem())
        }

        return try {
            val region = preferencesDataSource.tmdbRegion.first()
            val id = contentId.toIntOrNull() ?: return Result.Error(ApiError(message = "Invalid ID"))
            val dto = if (contentType == ContentType.MOVIE) tmdbApi.getMovieDetails(id) else tmdbApi.getTvDetails(id)
            val item = dto.toDomainItem(contentType, region)
            contentDao.insertContent(item.toEntity()) // Save full item
            Result.Success(item)
        } catch (e: Exception) {
            // Fallback to basic cache if network fails completely
            if (cachedEntity != null) Result.Success(cachedEntity.toDomainItem()) else Result.Error(ApiError.fromThrowable(e))
        }
    }

    suspend fun search(query: String, contentType: ContentType? = null): Result<out List<ContentItem>> {
        return try {
            val movieResults = if (contentType == null || contentType == ContentType.MOVIE) {
                tmdbApi.searchMovie(query).results ?: emptyList()  // FIX #3: Handle nullable
            } else {
                emptyList()
            }

            val tvResults = if (contentType == null || contentType == ContentType.SERIES) {
                tmdbApi.searchTv(query).results ?: emptyList()  // FIX #4: Handle nullable
            } else {
                emptyList()
            }

            val allResults = (movieResults + tvResults)
                .mapNotNull { it.toDomainItem() }
                .sortedByDescending { it.popularity ?: 0f }
            Result.Success(allResults)
        } catch (e: Exception) {
            Result.Error(ApiError.fromThrowable(e))
        }
    }

    suspend fun getSeasonEpisodes(seriesId: String, seasonNumber: Int): Result<out List<Episode>> {
        return try {
            val id = seriesId.toIntOrNull() ?: return Result.Error(ApiError(message = "Invalid Series ID"))
            val response = tmdbApi.getSeasonDetails(id, seasonNumber)
            val episodes = response.episodes.map { it.toEpisode(seriesId, seasonNumber) }
            Result.Success(episodes)
        } catch (e: Exception) {
            Result.Error(ApiError.fromThrowable(e))
        }
    }

    suspend fun getTopRated(contentType: ContentType, page: Int = 1): Result<List<ContentItem>> {
        val response = if (contentType == ContentType.MOVIE) tmdbApi.getTopRatedMovies(page)
        else tmdbApi.getTopRatedTv(page)
        return Result.Success(response.results?.mapNotNull { it.toDomainItem() } ?: emptyList())
    }

    suspend fun getNowPlaying(page: Int = 1): Result<List<ContentItem>> {
        val response = tmdbApi.getNowPlayingMovies(page)
        return Result.Success(response.results?.mapNotNull { it.toDomainItem() } ?: emptyList())
    }

    suspend fun getUpcomingMovies(page: Int = 1): Result<List<ContentItem>> {
        val response = tmdbApi.getUpcomingMovies(page)
        return Result.Success(response.results?.mapNotNull { it.toDomainItem() } ?: emptyList())
    }

    suspend fun getCurrentlyAiring(page: Int = 1): Result<List<ContentItem>> {
        val response = tmdbApi.getCurrentlyAiringTv(page)
        return Result.Success(response.results?.mapNotNull { it.toDomainItem() } ?: emptyList())
    }

    suspend fun getMoviesByGenre(genreId: Int? = null, page: Int = 1): Result<List<ContentItem>> {
        val response = tmdbApi.discoverMovies(genreId, page)
        return Result.Success(response.results?.mapNotNull { it.toDomainItem() } ?: emptyList())
    }

    suspend fun getRecommendations(id: String, type: ContentType): Result<List<ContentItem>> {
        val intId = id.toIntOrNull() ?: return Result.Success(emptyList())
        val response = if (type == ContentType.MOVIE) tmdbApi.getMovieRecommendations(intId)
        else tmdbApi.getTvRecommendations(intId)
        return Result.Success(response.results?.mapNotNull { it.toDomainItem() } ?: emptyList())
    }

    // Fallbacks for anticipated (Trakt is preferred, but these provide data)
    suspend fun getUpcomingSeries(page: Int = 1): Result<List<ContentItem>> = getCurrentlyAiring(page)
    suspend fun getAnticipatedSeries(page: Int = 1): Result<List<ContentItem>> = getPopular(ContentType.SERIES, page)
}