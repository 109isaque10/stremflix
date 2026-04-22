package com.stremflix.core.repository

import com.stremflix.core.model.CastMember
import com.stremflix.core.model.Episode
import com.stremflix.core.model.MediaItem
import com.stremflix.core.model.MediaType
import com.stremflix.core.model.PlaybackPreferences
import com.stremflix.core.model.StreamSource
import com.stremflix.core.model.Trailer
import com.stremflix.core.model.WatchProgress
import kotlinx.coroutines.flow.Flow

interface MetadataRepository {
    fun trending(): Flow<List<MediaItem>>
    fun popular(): Flow<List<MediaItem>>
    fun search(query: String, moviesOnly: Boolean? = null): Flow<List<MediaItem>>
    suspend fun detail(tmdbId: Int, mediaType: MediaType): MediaItem
    suspend fun episodes(tmdbId: Int, season: Int): List<Episode>
    suspend fun cast(tmdbId: Int, mediaType: MediaType): List<CastMember>
    suspend fun similar(tmdbId: Int, mediaType: MediaType): List<MediaItem>
    suspend fun trailers(tmdbId: Int, mediaType: MediaType): List<Trailer>
}

interface StreamRepository {
    suspend fun resolveStreams(tmdbId: Int, mediaType: MediaType): List<StreamSource>
}

interface WatchProgressRepository {
    fun observeContinueWatching(): Flow<List<WatchProgress>>
    suspend fun update(progress: WatchProgress)
}

interface PreferencesRepository {
    fun observePlaybackPreferences(): Flow<PlaybackPreferences>
    suspend fun updatePlaybackPreferences(update: PlaybackPreferences)
    suspend fun updateTmdbKey(key: String)
    suspend fun updateTraktKey(key: String)
    suspend fun updateStremioManifestUrl(url: String)
    suspend fun tmdbKey(): String?
    suspend fun traktKey(): String?
    suspend fun stremioManifestUrl(): String?
}

interface TraktRepository {
    suspend fun fetchPersonalListRows(): Map<String, List<MediaItem>>
    suspend fun fetchPopularPublicLists(): List<String>
}
