package com.stremflix.data.repository

import com.stremflix.core.domain.model.ApiError
import com.stremflix.core.domain.model.IdType
import com.stremflix.core.domain.model.Result
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.model.ExternalIds
import com.stremflix.data.remote.StremioApi
import com.stremflix.data.model.Stream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamRepository @Inject constructor(
    private val stremioApi: StremioApi,
    private val preferencesDataSource: PreferencesDataSource
) {

    suspend fun getStreams(contentId: String, contentType: String, idType: IdType, externalIds: ExternalIds? = null): Result<out List<Stream>> {
        return try {
            // Stremio expects type (movie/series) and id (imdb/tmdb)
            // We might need to resolve the correct ID format based on idType
            val streamId = resolveStreamId(contentId, idType, externalIds)

            if (streamId == null) {
                return Result.Error(ApiError(message = "Could not resolve content ID"))
            }

            val response = stremioApi.getStreams(contentType, streamId)
            val streams = response.streams.mapNotNull { dto ->
                dto.url?.let {
                    Stream(
                        description = dto.description,
                        url = it,
                        quality = dto.quality ?: extractQuality(dto.title),
                        language = extractLanguage(dto.title), // Default
                        behaviorHints = dto.behaviorHints?.let { bh ->
                            com.stremflix.data.model.BehaviorHints(bh.bingeGroup, bh.notWebReady, bh.proxyHeaders?.request)
                        }
                    )
                }
            }
            Result.Success(streams)
        } catch (e: Exception) {
            Result.Error(com.stremflix.core.domain.model.ApiError.fromThrowable(e))
        }
    }

    /**
     * Resolves the correct stream ID based on user preference
     * @param contentId The primary ID (usually TMDB)
     * @param idType User's preferred ID type (IMDB or TMDB)
     * @param externalIds External IDs from the content item
     * @return The resolved ID in the correct format, or null if cannot resolve
     */
    private fun resolveStreamId(
        contentId: String,
        idType: IdType,
        externalIds: ExternalIds?
    ): String? {
        return when (idType) {
            IdType.IMDB -> {
                // User prefers IMDB
                val imdbId = externalIds?.imdbId

                if (!imdbId.isNullOrEmpty()) {
                    if (imdbId.startsWith("tt")) imdbId else "tt$imdbId"
                } else {
                    // Try to construct from contentId if it looks like IMDB
                    if (contentId.startsWith("tt")) {
                        contentId
                    } else {
                        // Try TMDB format as last resort
                        externalIds?.tmdbId?.let { tmdb ->
                            "tmdb:$tmdb"
                        }
                    }
                }
            }

            IdType.TMDB -> {
                // User prefers TMDB
                val tmdbId = externalIds?.tmdbId

                if (tmdbId != null && tmdbId > 0) {
                    "tmdb:$tmdbId"
                } else {
                    // Try to parse contentId as number
                    contentId.toIntOrNull()?.let { num ->
                        "tmdb:$num"
                    } ?: externalIds?.imdbId?.let { imdb ->
                        imdb
                    }
                }
            }
        }
    }

    private fun extractQuality(title: String?): String? {
        return title?.let {
            val qualityPattern = Regex("(\\d{3,4}p|4K|HD|HDR)", RegexOption.IGNORE_CASE)
            qualityPattern.find(it)?.value
        }
    }

    private fun extractLanguage(title: String?): String? {
        // Extract language from stream title if present
        return title?.let {
            val langPattern = Regex("\\b(en|es|fr|de|pt|it|ja|ko|ru|zh)\\b", RegexOption.IGNORE_CASE)
            langPattern.find(it)?.value?.lowercase()
        }
    }
}