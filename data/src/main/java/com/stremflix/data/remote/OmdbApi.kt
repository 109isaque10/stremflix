package com.stremflix.data.remote

import com.stremflix.core.util.ApiEndpoints
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.remote.dto.omdb.OmdbResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OmdbApi @Inject constructor(
    private val httpClient: HttpClient,
    private val preferencesDataSource: PreferencesDataSource
) {
    private val apiKeyFlow: Flow<String?> = preferencesDataSource.omdbApiKey

    suspend fun getRatings(imdbId: String): OmdbResponse? {
        val apiKey = apiKeyFlow.first() ?: return null
        return try {
            httpClient.get(ApiEndpoints.OMDB_BASE) {
                parameter("i", imdbId)
                parameter("apikey", apiKey)
                parameter("plot", "short")
            }.body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getByTitle(title: String, year: String? = null): OmdbResponse? {
        val apiKey = apiKeyFlow.first() ?: return null
        return try {
            httpClient.get(ApiEndpoints.OMDB_BASE) {
                parameter("t", title)
                year?.let { parameter("y", it) }
                parameter("apikey", apiKey)
                parameter("plot", "short")
            }.body()
        } catch (e: Exception) {
            null
        }
    }
}