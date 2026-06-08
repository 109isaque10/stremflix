package com.stremflix.data.repository

import com.stremflix.core.domain.model.ApiError
import com.stremflix.core.domain.model.IdType
import com.stremflix.core.domain.model.Result
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.model.*
import com.stremflix.data.remote.StremioApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamRepository @Inject constructor(
    private val stremioApi: StremioApi,
    private val preferencesDataSource: PreferencesDataSource
) {

    suspend fun getStreams(
        contentId: String,
        contentType: String,
        idType: IdType,
        externalIds: ExternalIds? = null,
        season: Int? = null,
        episode: Int? = null
    ): Result<out List<Stream>> {
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
                    val combinedText =
                        "${dto.name} ${dto.title} ${dto.description} ${dto.behaviorHints?.filename}".lowercase()

                    Stream(
                        description = dto.description,
                        url = it,
                        quality = extractQuality(combinedText),
                        language = extractLanguage(combinedText), // Default
                        source = extractSource(combinedText),
                        extra = extractExtra(combinedText),
                        behaviorHints = dto.behaviorHints?.let { bh ->
                            com.stremflix.data.model.BehaviorHints(
                                bh.bingeGroup,
                                bh.notWebReady,
                                bh.proxyHeaders?.request
                            )
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

    private fun extractQuality(text: String): StreamQuality {
        return when {
            text.contains("2160p") || text.contains("4k") || text.contains("uhd") -> StreamQuality.P_2160
            text.contains("1080p") || text.contains("fhd") -> StreamQuality.P_1080
            text.contains("720p") || text.contains("hd") -> StreamQuality.P_720
            text.contains("480p") || text.contains("sd") -> StreamQuality.P_480
            else -> StreamQuality.UNKNOWN
        }
    }

    private fun extractSource(text: String): StreamSource {
        // Source tags are often in the name or filename hints. We check for common indicators in order of quality.
        val masterKeywords = arrayOf("remux", "bdmv")
        val highKeywords = arrayOf("blu-ray", "bd-rip", "br-rip", "bluray", "brrip", "bdrip", "hddvd")
        val mediumKeywords = arrayOf("dvdrip", "dvd-rip", "webrip", "web-rip", "dvd", "web", "web-dl", "webdl", "nf", "amzn")
        val lowKeywords = arrayOf("screener", "scr", "tvrip", "tv-rip", "hdtv", "pdtv")
        val worstKeywords = arrayOf("cam", "telecine", "tc", "hdcam", "hd-ts", "hdtc", "hdcamrip", "hdts", "camrip", "cam-rip", "telesync", "ts", "workprint", "wp")

        return when {
            masterKeywords.any { text.contains(it) } -> StreamSource.MASTER_QUALITY
            highKeywords.any { text.contains(it) } -> StreamSource.HIGH_QUALITY
            mediumKeywords.any { text.contains(it) } -> StreamSource.MEDIUM_QUALITY
            lowKeywords.any { text.contains(it) } -> StreamSource.LOW_QUALITY
            worstKeywords.any { text.contains(it) } -> StreamSource.WORST_QUALITY
            else -> StreamSource.UNKNOWN
        }
    }

    private fun extractExtra(text: String): Set<StreamExtra> {
        val streamExtra = mutableSetOf<StreamExtra>()
        if (text.contains("4k") || text.contains("uhd") || text.contains("2160p")) streamExtra.add(
            StreamExtra.FOUR_K
        )
        if (text.contains("5.1") || text.contains("5_1") || text.contains("5-1")) streamExtra.add(StreamExtra.FIVE_POINT_ONE)
        if (text.contains("7.1") || text.contains("7_1") || text.contains("7-1")) streamExtra.add(StreamExtra.SEVEN_POINT_ONE)
        if (arrayOf("hdr", "hdr10", "hdr10+", "dolby vision", "dv", "hdr10plus").any { text.contains(it) }) streamExtra.add(StreamExtra.HDR)
        if (text.contains("dolby vision") || text.contains("dv")) streamExtra.add(StreamExtra.DOLBY_VISION) // DV implies HDR
        when {
            arrayOf("dolby digital plus", "dd+", "dd plus", "ddp", "e-ac3").any {text.contains(it)} -> streamExtra.add(StreamExtra.DOLBY_DIGITAL_PLUS)
            arrayOf("dolby digital", "dd", "ac3").any {text.contains(it)} -> streamExtra.add(StreamExtra.DOLBY_DIGITAL)
        }
        when {
            text.contains("atmos") && text.contains("dolby vision") -> streamExtra.add(StreamExtra.ATMOS_VISION)
            text.contains("atmos") -> streamExtra.add(StreamExtra.ATMOS)
        }
        when {
            text.contains("imax") && text.contains("enhanced") -> streamExtra.add(StreamExtra.IMAX_ENHANCED)
            text.contains("imax") -> streamExtra.add(StreamExtra.IMAX)
        }
        when {
            text.contains("hdr10+") -> streamExtra.add(StreamExtra.HDR10_PLUS)
            text.contains("hdr10") -> streamExtra.add(StreamExtra.HDR10)
        }
        if (text.contains("truehd")) streamExtra.add(StreamExtra.TRUE_HD)
        when {
            text.contains("dts-x") || text.contains("dtsx") -> streamExtra.add(StreamExtra.DTS_X)
            text.contains("dts") -> streamExtra.add(StreamExtra.DTS)
        }
        return streamExtra
    }

    private fun extractLanguage(text: String): String {
        // Checked in order of priority. "Dual" overrides "Dublado".
        return when {
            text.contains("dual") -> "Dual Audio"
            text.contains("multi") -> "Multi Audio"
            text.contains("dublado") || text.contains(" dub ") || text.contains("pt-br") || text.contains("ptbr") || text.contains(
                "brazillian"
            ) || text.contains("nacional") -> "Dublado"

            text.contains("legendado") || text.contains(" leg ") -> "Legendado"
            text.contains(" en ") || text.contains("english") -> "English"
            text.contains(" es ") || text.contains("spanish") || text.contains("latino") -> "Spanish"
            else -> "Original" // Safe default if no tags are found
        }
    }
}
