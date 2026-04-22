package com.stremflix.data.repository

import com.stremflix.core.model.CastMember
import com.stremflix.core.model.Episode
import com.stremflix.core.model.MediaItem
import com.stremflix.core.model.MediaType
import com.stremflix.core.model.StreamSource
import com.stremflix.core.model.Trailer
import com.stremflix.core.model.WatchProgress
import com.stremflix.core.repository.MetadataRepository
import com.stremflix.core.repository.PreferencesRepository
import com.stremflix.core.repository.StreamRepository
import com.stremflix.core.repository.TraktRepository
import com.stremflix.core.repository.WatchProgressRepository
import com.stremflix.data.db.dao.CacheDao
import com.stremflix.data.db.entity.MediaEntity
import com.stremflix.data.db.entity.WatchProgressEntity
import com.stremflix.data.mapper.toCast
import com.stremflix.data.mapper.toMediaItem
import com.stremflix.data.mapper.toSubtitleTracks
import com.stremflix.data.mapper.toTrailer
import com.stremflix.data.mapper.toUrlAndHeaders
import com.stremflix.data.network.TmdbService
import com.stremflix.data.network.TraktService
import com.stremflix.data.network.dto.StremioManifestDto
import com.stremflix.data.network.dto.StremioStreamsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale

private const val DAY_MS = 24 * 60 * 60 * 1000L

class MetadataRepositoryImpl(
    private val tmdbService: TmdbService,
    private val cacheDao: CacheDao,
    private val preferencesRepository: PreferencesRepository,
    private val json: Json
) : MetadataRepository {

    override fun trending(): Flow<List<MediaItem>> = flow {
        emit(readListWithTtl("trending") {
            val key = preferencesRepository.tmdbKey().orEmpty()
            tmdbService.trending(key, Locale.getDefault().toLanguageTag()).results.map { it.toMediaItem() }
        })
    }

    override fun popular(): Flow<List<MediaItem>> = flow {
        emit(readListWithTtl("popular") {
            val key = preferencesRepository.tmdbKey().orEmpty()
            tmdbService.popularMovies(key, Locale.getDefault().toLanguageTag()).results.map { it.toMediaItem() }
        })
    }

    override fun search(query: String, moviesOnly: Boolean?): Flow<List<MediaItem>> = flow {
        val key = preferencesRepository.tmdbKey().orEmpty()
        val values = tmdbService.search(key, Locale.getDefault().toLanguageTag(), query)
            .results
            .map { it.toMediaItem() }
            .filter { moviesOnly == null || (moviesOnly && it.type == MediaType.MOVIE) || (!moviesOnly && it.type == MediaType.SHOW) }
        emit(values)
    }

    override suspend fun detail(tmdbId: Int, mediaType: MediaType): MediaItem {
        return trending().let { flow ->
            flow.map { list -> list.firstOrNull { it.tmdbId == tmdbId } }.map { it ?: fallback(tmdbId, mediaType) }
        }.kotlinx.coroutines.flow.first()
    }

    /**
     * Temporary fallback while TMDB season details endpoint wiring is pending.
     * Returns a fixed-length mock list so episode UIs remain usable in debug
     * builds instead of failing hard with empty/null states.
     */
    override suspend fun episodes(tmdbId: Int, season: Int): List<Episode> = List(8) {
        // TODO replace with TMDB season details endpoint wiring.
        Episode(
            season = season,
            episodeNumber = it + 1,
            title = "Episode ${it + 1}",
            runtimeMinutes = 45,
            overview = "Episode synopsis placeholder.",
            stillUrl = null
        )
    }

    override suspend fun cast(tmdbId: Int, mediaType: MediaType): List<CastMember> {
        val key = preferencesRepository.tmdbKey().orEmpty()
        val type = if (mediaType == MediaType.SHOW) "tv" else "movie"
        return tmdbService.credits(type, tmdbId, key, Locale.getDefault().toLanguageTag()).cast.map { it.toCast() }
    }

    override suspend fun similar(tmdbId: Int, mediaType: MediaType): List<MediaItem> = trending().kotlinx.coroutines.flow.first().take(10)

    override suspend fun trailers(tmdbId: Int, mediaType: MediaType): List<Trailer> {
        val key = preferencesRepository.tmdbKey().orEmpty()
        val type = if (mediaType == MediaType.SHOW) "tv" else "movie"
        return tmdbService.videos(type, tmdbId, key, Locale.getDefault().toLanguageTag())
            .results
            .filter { it.site.equals("YouTube", ignoreCase = true) && it.type.contains("Trailer", true) }
            .map { it.toTrailer() }
    }

    private suspend fun readListWithTtl(cacheKey: String, remote: suspend () -> List<MediaItem>): List<MediaItem> {
        val now = System.currentTimeMillis()
        val cached = cacheDao.read(cacheKey)
        if (cached != null && now - cached.cachedAt < DAY_MS) {
            return json.decodeFromString(cached.payload)
        }
        val fresh = remote()
        cacheDao.put(MediaEntity(cacheKey, 0, MediaType.MOVIE, json.encodeToString(fresh), now))
        cacheDao.purgeStale(now - DAY_MS)
        return fresh
    }

    private fun fallback(tmdbId: Int, mediaType: MediaType) = MediaItem(
        tmdbId = tmdbId,
        type = mediaType,
        title = "Untitled",
        backdropUrl = null,
        posterUrl = null,
        logoUrl = null,
        overview = "",
        year = null,
        seasons = null,
        maturity = null
    )
}

class StreamRepositoryImpl(
    private val client: OkHttpClient,
    private val preferencesRepository: PreferencesRepository,
    private val json: Json
) : StreamRepository {

    override suspend fun resolveStreams(tmdbId: Int, mediaType: MediaType): List<StreamSource> {
        val manifestUrl = preferencesRepository.stremioManifestUrl() ?: return emptyList()
        val manifest = client.newCall(Request.Builder().url(manifestUrl).build()).execute().use {
            if (!it.isSuccessful) return emptyList()
            json.decodeFromString<StremioManifestDto>(it.body?.string().orEmpty())
        }
        if (manifest.resources.none { it.equals("stream", ignoreCase = true) || it.equals("streams", ignoreCase = true) }) return emptyList()

        val mediaPrefix = if (mediaType == MediaType.SHOW) "series" else "movie"
        val base = manifestUrl.substringBefore("/manifest.json")
        val streamUrl = "$base/stream/$mediaPrefix/$tmdbId.json"

        return client.newCall(Request.Builder().url(streamUrl).build()).execute().use {
            if (!it.isSuccessful) return emptyList()
            json.decodeFromString<StremioStreamsResponse>(it.body?.string().orEmpty()).streams.mapNotNull { dto ->
                val (url, headers) = dto.toUrlAndHeaders() ?: return@mapNotNull null
                StreamSource(
                    name = dto.name ?: dto.title ?: "Stream",
                    url = url,
                    headers = headers,
                    subtitles = dto.toSubtitleTracks()
                )
            }
        }
    }
}

class WatchProgressRepositoryImpl(private val dao: CacheDao) : WatchProgressRepository {
    override fun observeContinueWatching(): Flow<List<WatchProgress>> = dao.observeWatchProgress().map { items ->
        items.map {
            WatchProgress(
                tmdbId = it.tmdbId,
                mediaType = it.mediaType,
                season = it.season,
                episode = it.episode,
                progressMs = it.progressMs,
                durationMs = it.durationMs,
                lastUpdated = it.lastUpdated
            )
        }
    }

    override suspend fun update(progress: WatchProgress) {
        dao.upsertWatchProgress(
            WatchProgressEntity(
                key = "${progress.mediaType}:${progress.tmdbId}:${progress.season}:${progress.episode}",
                tmdbId = progress.tmdbId,
                mediaType = progress.mediaType,
                season = progress.season,
                episode = progress.episode,
                progressMs = progress.progressMs,
                durationMs = progress.durationMs,
                lastUpdated = progress.lastUpdated
            )
        )
    }
}

class TraktRepositoryImpl(
    private val traktService: TraktService,
    private val preferencesRepository: PreferencesRepository
) : TraktRepository {
    override suspend fun fetchPersonalListRows(): Map<String, List<MediaItem>> {
        val key = preferencesRepository.traktKey().orEmpty()
        if (key.isBlank()) return emptyMap()
        return runCatching { traktService.lists(key) }.getOrDefault(emptyList()).associate { it.name to emptyList() }
    }

    override suspend fun fetchPopularPublicLists(): List<String> = listOf("Trending", "Top Rated", "Cult Classics")
}
