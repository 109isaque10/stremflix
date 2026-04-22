package com.stremflix.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stremflix.data.db.entity.MediaEntity
import com.stremflix.data.db.entity.WatchProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CacheDao {
    @Query("SELECT * FROM media_cache WHERE key = :key LIMIT 1")
    suspend fun read(key: String): MediaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: MediaEntity)

    @Query("DELETE FROM media_cache WHERE cachedAt < :expireBefore")
    suspend fun purgeStale(expireBefore: Long)

    @Query("SELECT * FROM watch_progress ORDER BY lastUpdated DESC")
    fun observeWatchProgress(): Flow<List<WatchProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchProgress(entity: WatchProgressEntity)
}
