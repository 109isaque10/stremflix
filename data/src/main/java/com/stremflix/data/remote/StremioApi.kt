package com.stremflix.data.remote

import com.stremflix.data.remote.dto.stremio.StremioStreamResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow
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
        return httpClient.get("${baseUrl}stream/${type}/${id}.json").body()
    }
}