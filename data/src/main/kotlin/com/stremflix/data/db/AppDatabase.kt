package com.stremflix.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stremflix.data.db.dao.CacheDao
import com.stremflix.data.db.entity.MediaEntity
import com.stremflix.data.db.entity.WatchProgressEntity

@Database(
    entities = [MediaEntity::class, WatchProgressEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}
