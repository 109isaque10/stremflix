// data/src/main/java/com/stremflix/data/remote/TmdbApi.kt

package com.stremflix.data.remote

import com.stremflix.core.util.ApiEndpoints
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.remote.dto.tmdb.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbApi @Inject constructor(
    private val httpClient: HttpClient,
    private val preferencesDataSource: PreferencesDataSource
) {
    private val apiKeyFlow: Flow<String> = preferencesDataSource.tmdbApiKey
    private val langFlow: Flow<String> = preferencesDataSource.tmdbLanguage
    private val json = Json { ignoreUnknownKeys = true }

    // Helper to check if API key is configured
    private suspend fun isApiKeyConfigured(): Boolean {
        val apiKey = apiKeyFlow.first()
        return apiKey.isNotEmpty() && apiKey != "YOUR_TMDB_API_KEY"
    }

    // Helper to handle error responses
    private suspend fun handleErrorResponse(response: HttpResponse): TmdbErrorDto? {
        return try {
            val text = response.bodyAsText()
            json.decodeFromString<TmdbErrorDto>(text)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getTrending(mediaType: String, timeWindow: String, page: Int = 1): TmdbPagedResponse {
        // Check API key first
        if (!isApiKeyConfigured()) {
            return TmdbPagedResponse(
                page = page,
                results = emptyList(),
                totalPages = 0,
                totalResults = 0
            )
        }

        val apiKey = apiKeyFlow.first()
        val lang = langFlow.first()

        return try {
            val response = httpClient.get("${ApiEndpoints.TMDB_BASE}trending/$mediaType/$timeWindow") {
                parameter("api_key", apiKey)
                parameter("page", page)
                parameter("language", lang)
            }

            when (response.status) {
                HttpStatusCode.OK -> response.body()
                HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> {
                    val error = handleErrorResponse(response)
                    println("TMDB API Error: ${error?.statusMessage ?: "Unauthorized"}")
                    TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0)
                }
                else -> {
                    println("TMDB API Error: ${response.status}")
                    TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0)
                }
            }
        } catch (e: Exception) {
            println("TMDB API Exception: ${e.message}")
            TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0)
        }
    }

    suspend fun getMovieDetails(movieId: Int): TmdbDetailsDto {
        if (!isApiKeyConfigured()) {
            return TmdbDetailsDto(
                id = movieId,
                title = null,
                name = null,
                overview = null,
                popularity = null,
                posterPath = null,
                backdropPath = null,
                releaseDate = null,
                firstAirDate = null,
                voteAverage = null,
                runtime = null,
                episodeRunTime = null,
                genres = null,
                credits = null,
                externalIds = null,
                videos = null,
                releaseDates = null,
                contentRatings = null
            )
        }

        val apiKey = apiKeyFlow.first()
        val lang = langFlow.first()

        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}movie/$movieId") {
                parameter("api_key", apiKey)
                parameter("append_to_response", "credits,videos,external_ids,release_dates,runtime,images")
                parameter("language", "$lang,en-US")
            }.body()
        } catch (e: Exception) {
            TmdbDetailsDto(
                id = movieId,
                title = "Error",
                name = null,
                overview = "Failed to load details",
                popularity = null,
                posterPath = null,
                backdropPath = null,
                releaseDate = null,
                firstAirDate = null,
                voteAverage = null,
                runtime = null,
                episodeRunTime = null,
                genres = null,
                credits = null,
                externalIds = null,
                videos = null,
                releaseDates = null,
                contentRatings = null
            )
        }
    }

    suspend fun getTvDetails(tvId: Int): TmdbDetailsDto {
        if (!isApiKeyConfigured()) {
            return TmdbDetailsDto(
                id = tvId,
                title = null,
                name = null,
                overview = null,
                posterPath = null,
                popularity = null,
                backdropPath = null,
                releaseDate = null,
                firstAirDate = null,
                voteAverage = null,
                runtime = null,
                episodeRunTime = null,
                genres = null,
                credits = null,
                externalIds = null,
                videos = null,
                releaseDates = null,
                contentRatings = null
            )
        }

        val apiKey = apiKeyFlow.first()
        val lang = langFlow.first()

        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}tv/$tvId") {
                parameter("api_key", apiKey)
                parameter("append_to_response", "credits,videos,images,external_ids,content_ratings,runtime,number_of_seasons")
                parameter("language", "$lang,en-US")
            }.body()
        } catch (e: Exception) {
            TmdbDetailsDto(
                id = tvId,
                title = null,
                name = "Error",
                overview = "Failed to load details",
                posterPath = null,
                popularity = null,
                backdropPath = null,
                releaseDate = null,
                firstAirDate = null,
                voteAverage = null,
                runtime = null,
                episodeRunTime = null,
                genres = null,
                credits = null,
                externalIds = null,
                videos = null,
                releaseDates = null,
                contentRatings = null
            )
        }
    }

    suspend fun getSeasonDetails(tvId: Int, seasonNumber: Int): TmdbSeasonDetailsDto {
        if (!isApiKeyConfigured()) return TmdbSeasonDetailsDto(id = 0, name = null, seasonNumber = seasonNumber)

        return httpClient.get("${ApiEndpoints.TMDB_BASE}tv/$tvId/season/$seasonNumber") {
            parameter("api_key", apiKeyFlow.first())
            parameter("language", langFlow.first())
        }.body()
    }

    suspend fun searchMovie(query: String, page: Int = 1): TmdbPagedResponse {
        if (!isApiKeyConfigured()) {
            return TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0)
        }

        val apiKey = apiKeyFlow.first()
        val lang = langFlow.first()

        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}search/movie") {
                parameter("api_key", apiKey)
                parameter("query", query)
                parameter("page", page)
                parameter("include_adult", "false")
                parameter("language", lang)
            }.body()
        } catch (e: Exception) {
            TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0)
        }
    }

    suspend fun searchTv(query: String, page: Int = 1): TmdbPagedResponse {
        if (!isApiKeyConfigured()) {
            return TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0)
        }

        val apiKey = apiKeyFlow.first()
        val lang = langFlow.first()

        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}search/tv") {
                parameter("api_key", apiKey)
                parameter("query", query)
                parameter("page", page)
                parameter("language", lang)
            }.body()
        } catch (e: Exception) {
            TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0)
        }
    }

    suspend fun getPopularMovies(page: Int = 1): TmdbPagedResponse {
        if (!isApiKeyConfigured()) {
            return TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0)
        }

        val apiKey = apiKeyFlow.first()
        val lang = langFlow.first()

        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}movie/popular") {
                parameter("api_key", apiKey)
                parameter("page", page)
                parameter("language", lang)
            }.body()
        } catch (e: Exception) {
            TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0)
        }
    }

    suspend fun getPopularTv(page: Int = 1): TmdbPagedResponse {
        if (!isApiKeyConfigured()) {
            return TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0)
        }

        val apiKey = apiKeyFlow.first()
        val lang = langFlow.first()

        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}tv/popular") {
                parameter("api_key", apiKey)
                parameter("page", page)
                parameter("language", lang)
            }.body()
        } catch (e: Exception) {
            TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0)
        }
    }

    suspend fun getMovieImages(movieId: Int): List<com.stremflix.data.remote.dto.tmdb.TmdbImageDto> {
        if (!isApiKeyConfigured()) return emptyList()

        val apiKey = apiKeyFlow.first()
        val lang = langFlow.first()

        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}movie/$movieId/images") {
                parameter("api_key", apiKey)
                parameter("language", lang)
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTvImages(tvId: Int): List<com.stremflix.data.remote.dto.tmdb.TmdbImageDto> {
        if (!isApiKeyConfigured()) return emptyList()

        val apiKey = apiKeyFlow.first()
        val lang = langFlow.first()

        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}tv/$tvId/images") {
                parameter("api_key", apiKey)
                parameter("language", lang)
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun discoverMovies(genre: Int?, page: Int = 1): TmdbPagedResponse {
        val apiKey = apiKeyFlow.first()
        val lang = langFlow.first()
        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}discover/movie") {
                parameter("api_key", apiKey)
                parameter("with_genres", genre)
                parameter("sort_by", "popularity.desc")
                parameter("page", page)
                parameter("language", lang)
            }.body()
        } catch (e: Exception) { TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0) }
    }

    suspend fun discoverTvShows(genre: Int?, page: Int = 1): TmdbPagedResponse {
        val apiKey = apiKeyFlow.first()
        val lang = langFlow.first()
        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}discover/tv") {
                parameter("api_key", apiKey)
                parameter("with_genres", genre)
                parameter("sort_by", "popularity.desc")
                parameter("page", page)
                parameter("language", lang)
            }.body()
        } catch (e: Exception) { TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0) }
    }

    // Add these methods to TmdbApi.kt

// ============ TOP RATED ============

    suspend fun getTopRatedMovies(page: Int = 1): TmdbPagedResponse {
        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}movie/top_rated") {
                parameter("api_key", apiKeyFlow.first())
                parameter("language", langFlow.first())
                parameter("page", page)
            }.body()
        } catch (e: Exception) { TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0) }
    }

    suspend fun getTopRatedTv(page: Int = 1): TmdbPagedResponse {
        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}tv/top_rated") {
                parameter("api_key", apiKeyFlow.first())
                parameter("language", langFlow.first())
                parameter("page", page)
            }.body()
        } catch (e: Exception) { TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0) }
    }

// ============ NOW PLAYING ============

    suspend fun getNowPlayingMovies(page: Int = 1): TmdbPagedResponse {
        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}movie/now_playing") {
                parameter("api_key", apiKeyFlow.first())
                parameter("language", langFlow.first())
                parameter("page", page)
            }.body()
        } catch (e: Exception) { TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0) }
    }

// ============ UPCOMING ============

    suspend fun getUpcomingMovies(page: Int = 1): TmdbPagedResponse {
        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}movie/upcoming") {
                parameter("api_key", apiKeyFlow.first())
                parameter("language", langFlow.first())
                parameter("page", page)
            }.body()
        } catch (e: Exception) { TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0) }
    }

// ============ CURRENTLY AIRING ============

    suspend fun getCurrentlyAiringTv(page: Int = 1): TmdbPagedResponse {
        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}tv/airing_today") {
                parameter("api_key", apiKeyFlow.first())
                parameter("language", langFlow.first())
                parameter("page", page)
            }.body()
        } catch (e: Exception) { TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0) }
    }

// ============ RECOMMENDATIONS ============

    suspend fun getMovieRecommendations(movieId: Int, page: Int = 1): TmdbPagedResponse {
        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}movie/$movieId/recommendations") {
                parameter("api_key", apiKeyFlow.first())
                parameter("language", langFlow.first())
                parameter("page", page)
            }.body()
        } catch (e: Exception) { TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0) }
    }

    suspend fun getTvRecommendations(tvId: Int, page: Int = 1): TmdbPagedResponse {
        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}tv/$tvId/recommendations") {
                parameter("api_key", apiKeyFlow.first())
                parameter("language", langFlow.first())
                parameter("page", page)
            }.body()
        } catch (e: Exception) { TmdbPagedResponse(page = page, results = emptyList(), totalPages = 0, totalResults = 0) }
    }

// ============ GENRES ============

    suspend fun getMovieGenres(): GenreResponse {
        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}genre/movie/list") {
                parameter("api_key", apiKeyFlow.first())
                parameter("language", langFlow.first())
            }.body()
        } catch (e: Exception) { GenreResponse(emptyList()) }
    }

    suspend fun getTvGenres(): GenreResponse {
        return try {
            httpClient.get("${ApiEndpoints.TMDB_BASE}genre/tv/list") {
                parameter("api_key", apiKeyFlow.first())
                parameter("language", langFlow.first())
            }.body()
        } catch (e: Exception) { GenreResponse(emptyList()) }
    }

    // Add this DTO
    @Serializable
    data class GenreResponse(
        val genres: List<GenreDto>
    )
}