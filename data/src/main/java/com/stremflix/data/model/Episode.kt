package com.stremflix.data.model

data class Episode(
    val seriesId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val synopsis: String?,
    val thumbnailUrl: String?,
    val runtime: Int?,
    val releaseDate: kotlinx.datetime.LocalDate?,
    val streamUrl: String?,
    val watched: Boolean,
    val watchProgress: Float,
    val videoUrl: String? = null // From Stremio stream
) {
    val displayName: String
        get() = "S${seasonNumber.toString().padStart(2, '0')}E${episodeNumber.toString().padStart(2, '0')}: $title"
}