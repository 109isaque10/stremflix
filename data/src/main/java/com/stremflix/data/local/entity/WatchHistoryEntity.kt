// data/src/main/java/com/stremflix/data/local/entity/WatchHistoryEntity.kt

package com.stremflix.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val episodeId: String,
    val seriesId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val watched: Boolean,
    val watchProgress: Float,
    val lastWatchedAt: Long?,
    val title: String,
    val synopsis: String?
)