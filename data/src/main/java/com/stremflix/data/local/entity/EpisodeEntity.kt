package com.stremflix.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "episodes",
    primaryKeys = ["seriesId", "seasonNumber", "episodeNumber"]
)
data class EpisodeEntity(
    val seriesId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val synopsis: String?,
    val thumbnailUrl: String?,
    val runtime: Int?,
    val releaseDate: String?,
    val streamUrl: String?,
    val watched: Boolean,
    val watchProgress: Float,
    val videoUrl: String?
)