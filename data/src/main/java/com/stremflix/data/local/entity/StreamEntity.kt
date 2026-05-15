package com.stremflix.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streams")
data class StreamEntity(
    @PrimaryKey val id: String, // composite: contentId_type
    val contentId: String,
    val type: String, // "stream"
    val url: String,
    val quality: String?,
    val language: String?,
    val behaviorHintsJson: String?,
    val cachedAt: Long
)