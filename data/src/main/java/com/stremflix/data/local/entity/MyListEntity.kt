package com.stremflix.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_list")
data class MyListEntity(
    @PrimaryKey val id: String,  // TMDB or IMDB ID
    val type: String,  // "movie" or "tv"
    val title: String,
    val posterUrl: String?,
    val year: Int?,
    val dateAdded: Long,  // Timestamp when added to list
    val traktId: Int? = null,
    val tmdbId: Int? = null,
    val imdbId: String? = null
)