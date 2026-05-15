package com.stremflix.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trakt_tokens")
data class TraktTokenEntity(
    @PrimaryKey val id: Int = 1, // Singleton row
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long // epochMillis
)