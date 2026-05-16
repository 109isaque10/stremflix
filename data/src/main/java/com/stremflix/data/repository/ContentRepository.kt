package com.stremflix.data.repository

import com.stremflix.core.domain.model.ApiError
import com.stremflix.core.domain.model.ContentType
import com.stremflix.core.domain.model.Result
import com.stremflix.core.util.CacheConfig
import com.stremflix.core.util.cached
import com.stremflix.data.local.dao.ContentDao
import com.stremflix.data.local.entity.ContentEntity
import com.stremflix.data.mapper.toDomainItem
import com.stremflix.data.mapper.toEpisode
import com.stremflix.data.model.ContentItem
import com.stremflix.data.model.Episode
import com.stremflix.data.remote.TmdbApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val contentDao: ContentDao
) {

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
            val id = contentId.toIntOrNull() ?: return Result.Error(ApiError(message = "Invalid ID"))
            val dto = if (contentType == ContentType.MOVIE) tmdbApi.getMovieDetails(id) else tmdbApi.getTvDetails(id)
            val item = dto.toDomainItem(contentType)
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

    // data/src/main/java/com/stremflix/data/repository/ContentRepository.kt

    // Add these methods to ContentRepository class
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

private fun ContentItem.toEntity(): ContentEntity {
    return ContentEntity(
        id = id,
        type = type,
        title = title,
        year = year,
        popularity = popularity,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        rating = rating,
        synopsis = synopsis,
        genresJson = "", // Serialize genres to JSON
        castJson = "",   // Serialize cast to JSON
        runtime = runtime,
        matchScore = matchScore,
        releaseDate = releaseDate?.toString(),
        imdbId = externalIds.imdbId,
        tmdbId = externalIds.tmdbId,
        traktId = externalIds.traktId,
        tvdbId = externalIds.tvdbId,
        lastWatched = lastWatched,
        watchProgress = watchProgress
    )
}

private fun ContentEntity.toDomainItem(): ContentItem {
    return ContentItem(
        id = id,
        type = type,
        title = title,
        year = year,
        popularity = popularity,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        rating = rating,
        contentRating = null,
        synopsis = synopsis,
        genres = emptyList(), // Parse JSON
        cast = emptyList(),   // Parse JSON
        runtime = runtime,
        matchScore = matchScore,
        releaseDate = releaseDate?.let { try { kotlinx.datetime.LocalDate.parse(it) } catch (e: Exception) { null } },
        externalIds = com.stremflix.data.model.ExternalIds(imdbId, tmdbId, traktId, tvdbId),
        lastWatched = lastWatched,
        watchProgress = watchProgress
    )
}