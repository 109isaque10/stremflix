package com.stremflix.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stremflix.core.model.MediaType

@Entity(tableName = "media_cache")
data class MediaEntity(
    @PrimaryKey val key: String,
    val tmdbId: Int,
    val mediaType: MediaType,
    val payload: String,
    val cachedAt: Long
)

@Entity(tableName = "watch_progress")
data class WatchProgressEntity(
    @PrimaryKey val key: String,
    val tmdbId: Int,
    val mediaType: MediaType,
    val season: Int?,
    val episode: Int?,
    val progressMs: Long,
    val durationMs: Long,
    val lastUpdated: Long
)
