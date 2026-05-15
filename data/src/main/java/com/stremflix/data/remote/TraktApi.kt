// data/src/main/java/com/stremflix/data/remote/TraktApi.kt

package com.stremflix.data.remote

import com.stremflix.core.util.ApiEndpoints
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.remote.dto.trakt.*
import com.stremflix.data.repository.TraktTokenRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktApi @Inject constructor(
    private val httpClient: HttpClient,
    private val preferencesDataSource: PreferencesDataSource,
    private val tokenRepository: TraktTokenRepository
) {
    private val clientIdFlow: Flow<String> = preferencesDataSource.traktClientId

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun getHeaders(): HeadersBuilder.() -> Unit = {
        GlobalScope.launch(Dispatchers.IO) {
            val clientId = clientIdFlow.first()
            val token = tokenRepository.tokenFlow.first()?.accessToken

            append("trakt-api-version", "2")
            append(HttpHeaders.ContentType, "application/json")
            if (clientId.isNotEmpty()) append("trakt-api-key", clientId)
            if (token != null) append(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    // ============ AUTHENTICATION ============

    suspend fun exchangeCode(code: String, clientId: String, clientSecret: String, redirectUri: String): AccessTokenResponse {
        return httpClient.post("${ApiEndpoints.TRAKT_BASE}oauth/token") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "code" to code,
                    "client_id" to clientId,
                    "client_secret" to clientSecret,
                    "redirect_uri" to redirectUri,
                    "grant_type" to "authorization_code"
                )
            )
        }.body()
    }

    suspend fun refreshToken(refreshToken: String, clientId: String, clientSecret: String): AccessTokenResponse {
        return httpClient.post("${ApiEndpoints.TRAKT_BASE}oauth/token") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "refresh_token" to refreshToken,
                    "client_id" to clientId,
                    "client_secret" to clientSecret,
                    "grant_type" to "refresh_token"
                )
            )
        }.body()
    }

    suspend fun revokeToken(token: String, clientId: String, clientSecret: String) {
        httpClient.post("${ApiEndpoints.TRAKT_BASE}oauth/revoke") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "token" to token,
                    "client_id" to clientId,
                    "client_secret" to clientSecret
                )
            )
        }
    }

    // ============ WATCHLIST ============

    suspend fun getWatchlist(): List<TraktWatchlistItem> {
        return httpClient.get("${ApiEndpoints.TRAKT_BASE}sync/watchlist") {
            headers(getHeaders())
            parameter("extended", "full")
        }.body()
    }

    suspend fun addToWatchlistMovie(tmdbId: Int) {
        httpClient.post("${ApiEndpoints.TRAKT_BASE}sync/watchlist") {
            contentType(ContentType.Application.Json)
            headers(getHeaders())
            setBody(
                mapOf("movies" to listOf(mapOf("ids" to mapOf("tmdb" to tmdbId))))
            )
        }
    }

    suspend fun addToWatchlistShow(tmdbId: Int) {
        httpClient.post("${ApiEndpoints.TRAKT_BASE}sync/watchlist") {
            contentType(ContentType.Application.Json)
            headers(getHeaders())
            setBody(
                mapOf("shows" to listOf(mapOf("ids" to mapOf("tmdb" to tmdbId))))
            )
        }
    }

    suspend fun removeFromWatchlist(itemId: String) {
        httpClient.post("${ApiEndpoints.TRAKT_BASE}sync/watchlist/remove") {
            contentType(ContentType.Application.Json)
            headers(getHeaders())
            setBody(mapOf("ids" to listOf(itemId)))
        }
    }

    // ============ RECOMMENDATIONS ============

    suspend fun getMovieRecommendations(limit: Int = 20): List<TraktMovie> {
        return httpClient.get("${ApiEndpoints.TRAKT_BASE}recommendations/movies") {
            headers(getHeaders())
            parameter("limit", limit)
            parameter("extended", "full")
        }.body()
    }

    suspend fun getShowRecommendations(limit: Int = 20): List<TraktShow> {
        return httpClient.get("${ApiEndpoints.TRAKT_BASE}recommendations/shows") {
            headers(getHeaders())
            parameter("limit", limit)
            parameter("extended", "full")
        }.body()
    }

    // ============ TRENDING ============

    suspend fun getTrendingMovies(limit: Int = 20): List<TraktTrendingItem> {
        return httpClient.get("${ApiEndpoints.TRAKT_BASE}movies/trending") {
            headers(getHeaders())
            parameter("limit", limit)
            parameter("extended", "full")
        }.body()
    }

    suspend fun getTrendingShows(limit: Int = 20): List<TraktTrendingItem> {
        return httpClient.get("${ApiEndpoints.TRAKT_BASE}shows/trending") {
            headers(getHeaders())
            parameter("limit", limit)
            parameter("extended", "full")
        }.body()
    }

    // ============ MOST WATCHED ============

    suspend fun getMostWatchedMovies(period: String = "weekly", limit: Int = 20): List<TraktWatchedItem> {
        return httpClient.get("${ApiEndpoints.TRAKT_BASE}movies/watched/$period") {
            headers(getHeaders())
            parameter("limit", limit)
            parameter("extended", "full")
        }.body()
    }

    suspend fun getMostWatchedShows(period: String = "weekly", limit: Int = 20): List<TraktWatchedItem> {
        return httpClient.get("${ApiEndpoints.TRAKT_BASE}shows/watched/$period") {
            headers(getHeaders())
            parameter("limit", limit)
            parameter("extended", "full")
        }.body()
    }

    // ============ MOST ANTICIPATED ============

    suspend fun getMostAnticipatedMovies(limit: Int = 20): List<TraktAnticipatedItem> {
        return httpClient.get("${ApiEndpoints.TRAKT_BASE}movies/anticipated") {
            headers(getHeaders())
            parameter("limit", limit)
            parameter("extended", "full")
        }.body()
    }

    suspend fun getMostAnticipatedShows(limit: Int = 20): List<TraktAnticipatedItem> {
        return httpClient.get("${ApiEndpoints.TRAKT_BASE}shows/anticipated") {
            headers(getHeaders())
            parameter("limit", limit)
            parameter("extended", "full")
        }.body()
    }

    // ============ CALENDAR ============

    suspend fun getCalendar(limit: Int = 30): List<TraktCalendarItem> {
        return httpClient.get("${ApiEndpoints.TRAKT_BASE}calendars/my/shows") {
            headers(getHeaders())
            parameter("limit", limit)
            parameter("extended", "full")
        }.body()
    }

    suspend fun getCalendarNewShows(limit: Int = 30): List<TraktCalendarItem> {
        return httpClient.get("${ApiEndpoints.TRAKT_BASE}calendars/my/shows/new") {
            headers(getHeaders())
            parameter("limit", limit)
            parameter("extended", "full")
        }.body()
    }

    // ============ WATCHED HISTORY ============

    suspend fun getWatchedMovies(): List<TraktWatchedItem> {
        return httpClient.get("${ApiEndpoints.TRAKT_BASE}sync/watched/movies") {
            headers(getHeaders())
            parameter("extended", "full")
        }.body()
    }

    suspend fun getWatchedShows(): List<TraktWatchedShow> {
        return httpClient.get("${ApiEndpoints.TRAKT_BASE}sync/watched/shows") {
            headers(getHeaders())
            parameter("extended", "full")
        }.body()
    }

    suspend fun addWatchedHistory(item: TraktHistoryItem) {
        httpClient.post("${ApiEndpoints.TRAKT_BASE}sync/history") {
            contentType(ContentType.Application.Json)
            headers(getHeaders())
            setBody(item)
        }
    }

    suspend fun removeWatchedHistory(historyId: String) {
        httpClient.post("${ApiEndpoints.TRAKT_BASE}sync/history/remove") {
            contentType(ContentType.Application.Json)
            headers(getHeaders())
            setBody(mapOf("ids" to listOf(historyId)))
        }
    }

    // ============ SCROBBLING ============

    suspend fun scrobbleStart(item: TraktScrobbleItem) {
        httpClient.post("${ApiEndpoints.TRAKT_BASE}scrobble/start") {
            contentType(ContentType.Application.Json)
            headers(getHeaders())
            setBody(item)
        }
    }

    suspend fun scrobblePause(item: TraktScrobbleItem) {
        httpClient.post("${ApiEndpoints.TRAKT_BASE}scrobble/pause") {
            contentType(ContentType.Application.Json)
            headers(getHeaders())
            setBody(item)
        }
    }

    suspend fun scrobbleStop(item: TraktScrobbleItem) {
        httpClient.post("${ApiEndpoints.TRAKT_BASE}scrobble/stop") {
            contentType(ContentType.Application.Json)
            headers(getHeaders())
            setBody(item)
        }
    }

    // ============ USER PROFILE ============

    suspend fun getUserProfile(): TraktUser {
        return httpClient.get("${ApiEndpoints.TRAKT_BASE}users/me") {
            headers(getHeaders())
        }.body()
    }

    suspend fun getUserSettings(): TraktUserSettings {
        return httpClient.get("${ApiEndpoints.TRAKT_BASE}users/me/settings") {
            headers(getHeaders())
        }.body()
    }

    suspend fun getDeviceCode(clientId: String): DeviceCodeResponse {
        return httpClient.post("${ApiEndpoints.TRAKT_BASE}oauth/device/code") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("client_id" to clientId))
        }.body()
    }

    suspend fun pollAccessToken(deviceCode: String, clientId: String, clientSecret: String): AccessTokenResponse {
        return httpClient.post("${ApiEndpoints.TRAKT_BASE}oauth/device/token") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "code" to deviceCode,
                    "client_id" to clientId,
                    "client_secret" to clientSecret
                )
            )
        }.body()
    }
}