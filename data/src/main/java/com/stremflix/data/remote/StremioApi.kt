package com.stremflix.data.remote

import com.stremflix.data.remote.dto.stremio.StremioStreamResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StremioApi @Inject constructor(
    private val httpClient: HttpClient,
    private val preferencesDataSource: com.stremflix.data.local.PreferencesDataSource
) {
    private val baseUrlFlow: Flow<String> = preferencesDataSource.stremioBaseUrl

    suspend fun getStreams(type: String, id: String): StremioStreamResponse {
        val baseUrl = baseUrlFlow.first()
        return httpClient.get("${baseUrl}stream/${type}/${id}.json") {
            parameter("language", "en")
        }.body()
    }

    suspend fun getCatalog(type: String, id: String, skip: Int = 0): List<com.stremflix.data.remote.dto.tmdb.TmdbContentDto> {
        val baseUrl = baseUrlFlow.first()
        return httpClient.get("${baseUrl}catalog/${type}/${id}.json") {
            parameter("skip", skip)
        }.body()
    }

    suspend fun getMeta(type: String, id: String): com.stremflix.data.remote.dto.tmdb.TmdbDetailsDto {
        val baseUrl = baseUrlFlow.first()
        return httpClient.get("${baseUrl}meta/${type}/${id}.json").body()
    }
}