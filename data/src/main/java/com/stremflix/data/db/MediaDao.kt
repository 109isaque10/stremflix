package com.stremflix.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stremflix.core.model.MediaItem

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MediaItem)

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaItem(id: String): MediaItem?

    @Query("SELECT * FROM media_items WHERE type = :type ORDER BY lastUpdated DESC")
    suspend fun getPopularMedia(type: String): List<MediaItem>
    
    @Query("SELECT * FROM media_items")
    suspend fun getAllMedia(): List<MediaItem>
}