package com.stremflix.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class MediaType { MOVIE, SHOW }

@Serializable
data class MediaItem(
    val tmdbId: Int,
    val type: MediaType,
    val title: String,
    val backdropUrl: String?,
    val posterUrl: String?,
    val logoUrl: String?,
    val overview: String,
    val year: String?,
    val seasons: Int?,
    val maturity: String?,
    val quality: String = "4K Ultra HD",
    val audio: String = "5.1",
    val matchPercent: Int = 95,
    val genres: List<String> = emptyList(),
    val cast: List<String> = emptyList()
)

@Serializable
data class Episode(
    val season: Int,
    val episodeNumber: Int,
    val title: String,
    val runtimeMinutes: Int,
    val overview: String,
    val stillUrl: String?
)

@Serializable
data class CastMember(
    val name: String,
    val character: String,
    val imageUrl: String?
)

@Serializable
data class Trailer(
    val key: String,
    val site: String,
    val type: String
)

@Serializable
data class StreamSource(
    val name: String,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val subtitles: List<SubtitleTrack> = emptyList()
)

@Serializable
data class SubtitleTrack(
    val language: String,
    val url: String,
    val mimeType: String? = null
)

@Serializable
data class WatchProgress(
    val tmdbId: Int,
    val mediaType: MediaType,
    val season: Int? = null,
    val episode: Int? = null,
    val progressMs: Long,
    val durationMs: Long,
    val lastUpdated: Long
)

@Serializable
data class SubtitlePreferences(
    val fontSizeSp: Int = 18,
    val colorArgb: Long = 0xFFFFFFFF,
    val backgroundOpacity: Float = 0.35f,
    val outlineEnabled: Boolean = true,
    val foreignAudioOnly: Boolean = true
)

@Serializable
data class PlaybackPreferences(
    val subtitle: SubtitlePreferences = SubtitlePreferences(),
    val skipBackSeconds: Int = 10,
    val muteOnStartup: Boolean = false,
    val autoTrailerPreview: Boolean = true,
    val trailerPreviewTimeoutSeconds: Int = 5
)
