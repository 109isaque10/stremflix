package com.stremflix.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.stremflix.data.local.entity.ContentEntity
import com.stremflix.data.local.entity.EpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: ContentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentList(contents: List<ContentEntity>)

    @Update
    suspend fun updateContent(content: ContentEntity)

    @Query("SELECT * FROM content_items WHERE id = :id")
    suspend fun getContentById(id: String): ContentEntity?

    @Query("SELECT * FROM content_items WHERE id = :id")
    fun getContentByIdFlow(id: String): Flow<ContentEntity?>

    @Query("SELECT * FROM content_items WHERE watchProgress > 0 AND watchProgress < 1 ORDER BY lastWatched DESC")
    fun getContinueWatchingFlow(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content_items ORDER BY lastWatched DESC LIMIT :limit")
    fun getRecentFlow(limit: Int): Flow<List<ContentEntity>>

    @Query("DELETE FROM content_items WHERE lastWatched < :threshold")
    suspend fun cleanupOldContent(threshold: Long)
}