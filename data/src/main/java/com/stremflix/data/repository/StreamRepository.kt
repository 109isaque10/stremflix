package com.stremflix.data.repository

import com.stremflix.core.domain.model.ApiError
import com.stremflix.core.domain.model.IdType
import com.stremflix.core.domain.model.Result
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.model.ExternalIds
import com.stremflix.data.model.Stream
import com.stremflix.data.remote.StremioApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamRepository @Inject constructor(
    private val stremioApi: StremioApi,
    private val preferencesDataSource: PreferencesDataSource
) {

    suspend fun getStreams(contentId: String, contentType: String, idType: IdType, externalIds: ExternalIds? = null, season: Int? = null, episode: Int? = null): Result<out List<Stream>> {
        return try {
            // Stremio expects type (movie/series) and id (imdb/tmdb)
            // We might need to resolve the correct ID format based on idType
            val streamId = resolveStreamId(contentId, idType, externalIds, episode, season)

            if (streamId == null) {
                return Result.Error(ApiError(message = "Could not resolve content ID"))
            }

            val response = stremioApi.getStreams(contentType, streamId)
            val streams = response.streams.mapNotNull { dto ->
                dto.url?.let {
                    val combinedText = "${dto.name} ${dto.title} ${dto.description} ${dto.behaviorHints?.filename}".lowercase()

                    Stream(
                        description = dto.description,
                        url = it,
                        quality = extractQuality(combinedText),
                        language = extractLanguage(combinedText), // Default
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
        externalIds: ExternalIds?,
        episode: Int?,
        season: Int?
    ): String? {
        val suffix = if (season != null && episode != null) ":$season:$episode" else ""
        return when (idType) {
            IdType.IMDB -> {
                // User prefers IMDB
                val imdbId = externalIds?.imdbId

                if (!imdbId.isNullOrEmpty()) {
                    val formattedImdb = if (imdbId.startsWith("tt")) imdbId else "tt$imdbId"
                    "$formattedImdb$suffix"
                } else {
                    // Try to construct from contentId if it looks like IMDB
                    if (contentId.startsWith("tt")) {
                        "$contentId$suffix"
                    } else {
                        // Try TMDB format as last resort
                        externalIds?.tmdbId?.let { tmdb ->
                            "tmdb:$tmdb$suffix"
                        }
                    }
                }
            }

            IdType.TMDB -> {
                // User prefers TMDB
                val tmdbId = externalIds?.tmdbId

                if (tmdbId != null && tmdbId > 0) {
                    "tmdb:$tmdbId$suffix"
                } else {
                    // Try to parse contentId as number
                    contentId.toIntOrNull()?.let { num ->
                        "tmdb:$num$suffix"
                    } ?: externalIds?.imdbId?.let { imdb ->
                        val formattedImdb = if (imdb.startsWith("tt")) imdb else "tt$imdb"
                        "$formattedImdb$suffix"
                    }
                }
            }
        }
    }

    private fun extractQuality(text: String): String {
        return when {
            text.contains("2160p") || text.contains("4k") || text.contains("uhd") -> "4K"
            text.contains("1080p") || text.contains("fhd") -> "FHD"
            text.contains("720p") || text.contains("hd") -> "HD"
            text.contains("480p") || text.contains("sd") -> "SD"
            else -> "Unknown"
        }
    }

    private fun extractLanguage(text: String): String {
        // Checked in order of priority. "Dual" overrides "Dublado".
        return when {
            text.contains("dual") -> "dual audio"
            text.contains("multi") -> "multi audio"
            text.contains("dublado") || text.contains(" dub ") || text.contains("pt-br") || text.contains("ptbr")  || text.contains("brazillian") || text.contains("nacional") -> "Dublado"
            text.contains("legendado") || text.contains(" leg ") -> "Legendado"
            text.contains(" en ") || text.contains("english") -> "English"
            text.contains(" es ") || text.contains("spanish") || text.contains("latino") -> "Spanish"
            else -> "Original" // Safe default if no tags are found
        }
    }
}