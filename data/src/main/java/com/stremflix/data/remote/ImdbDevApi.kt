package com.stremflix.data.remote

import com.stremflix.core.util.ApiEndpoints
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImdbDevApi @Inject constructor(
    private val httpClient: HttpClient,
) {
    suspend fun getParentsGuide(imdbId: String): Map<String, String>? {
        return try {
            val url = ApiEndpoints.IMDB_BASE + "titles/${imdbId}/parentsGuide"
            httpClient.get(ApiEndpoints.IMDB_BASE) {
            }.body()
        } catch (e: Exception) {
            null
        }
    }
}