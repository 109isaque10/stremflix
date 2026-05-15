package com.stremflix.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stremflix.data.local.dao.ContentDao
import com.stremflix.data.local.dao.EpisodeDao
import com.stremflix.data.local.dao.TraktTokenDao
import com.stremflix.data.local.dao.WatchHistoryDao
import com.stremflix.data.local.entity.ContentEntity
import com.stremflix.data.local.entity.EpisodeEntity
import com.stremflix.data.local.entity.TraktTokenEntity
import com.stremflix.data.local.entity.WatchHistoryEntity
@Database(
    entities = [
        ContentEntity::class,
        EpisodeEntity::class,
        TraktTokenEntity::class,
        WatchHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StremflixDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun traktTokenDao(): TraktTokenDao
    abstract fun watchHistoryDao(): WatchHistoryDao
}