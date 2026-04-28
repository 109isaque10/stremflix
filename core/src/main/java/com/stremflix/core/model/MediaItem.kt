package com.stremflix.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MediaType { MOVIE, SERIES }

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey val id: String, // TMDB ID
    val type: MediaType,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String,
    val releaseYear: String?,
    val matchPercent: Int,
    val maturityRating: String,
    val qualityTag: String = "HD",
    val cast: List<String> = emptyList(),
    val genres: List<String> = emptyList(),
    val lastUpdated: Long = 0
)

@Entity(tableName = "episodes")
data class Episode(
    @PrimaryKey val id: String,
    val seriesId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val overview: String,
    val durationMinutes: Int,
    val thumbnailPath: String?
)

data class StreamResult(
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val behaviorHints: Map<String, Any> = emptyMap()
)