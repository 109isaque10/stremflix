package com.stremflix.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stremflix.core.domain.model.ContentType

@Entity(tableName = "content_items")
data class ContentEntity(
    @PrimaryKey val id: String,
    val type: ContentType,
    val title: String,
    val year: Int?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val rating: Float?,
    val contentRating: String?,
    val synopsis: String?,
    val genresJson: String, // JSON string for List<String>
    val castJson: String,   // JSON string for List<String>
    val runtime: Int?,
    val matchScore: Int?,
    val releaseDate: String?, // ISO string
    val imdbId: String?,
    val tmdbId: Int?,
    val traktId: Int?,
    val tvdbId: Int?,
    val lastWatched: Long?,
    val watchProgress: Float,
    val popularity: Float?
)