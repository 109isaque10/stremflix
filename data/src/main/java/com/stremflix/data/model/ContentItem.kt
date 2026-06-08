package com.stremflix.data.model

import com.stremflix.core.domain.model.ContentType
import com.stremflix.data.remote.dto.tmdb.VideoDto
import kotlinx.datetime.LocalDate

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
    val trailerId: String? = null,
    val titleLogoUrl: String? = null,
    val videos: List<VideoDto>? = null,
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