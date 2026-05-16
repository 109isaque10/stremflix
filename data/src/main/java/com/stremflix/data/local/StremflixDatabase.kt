package com.stremflix.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stremflix.data.local.dao.*
import com.stremflix.data.local.entity.*

@Database(
    entities = [
        ContentEntity::class,
        EpisodeEntity::class,
        TraktTokenEntity::class,
        MyListEntity::class,
        WatchHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StremflixDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun traktTokenDao(): TraktTokenDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun myListDao(): MyListDao
}