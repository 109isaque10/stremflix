package com.stremflix.data.model

import com.stremflix.core.domain.model.ContentType
import kotlinx.datetime.LocalDate
import java.time.Instant

data class ContentItem(
    val id: String,
    val type: ContentType,
    val title: String,
    val year: Int?,
    val popularity: Float?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val rating: Float?,
    val contentRating: String?,
    val synopsis: String?,
    val genres: List<String>,
    val cast: List<String>,
    val runtime: Int?,
    val matchScore: Int?,
    val releaseDate: LocalDate?,
    val externalIds: ExternalIds,
    val lastWatched: Long?, // epochMillis
    val watchProgress: Float, // 0.0 - 1.0
    val numberOfSeasons: Int? = null
)

data class ExternalIds(
    val imdbId: String?,
    val tmdbId: Int?,
    val traktId: Int?,
    val tvdbId: Int?
)