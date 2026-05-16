// data/src/main/java/com/stremflix/data/local/dao/WatchHistoryDao.kt

package com.stremflix.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stremflix.data.local.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: WatchHistoryEntity)

    @Query("SELECT * FROM watch_history WHERE seriesId = :seriesId ORDER BY lastWatchedAt DESC LIMIT 1")
    suspend fun getLastWatchedForSeries(seriesId: String): WatchHistoryEntity?

    @Query("SELECT * FROM watch_history WHERE seriesId = :seriesId ORDER BY seasonNumber ASC, episodeNumber ASC")
    suspend fun getHistoryForSeries(seriesId: String): List<WatchHistoryEntity>

    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE seriesId = :seriesId AND seasonNumber = :seasonNumber AND episodeNumber = :episodeNumber")
    suspend fun getEpisode(seriesId: String, seasonNumber: Int, episodeNumber: Int): WatchHistoryEntity?

    @Query("DELETE FROM watch_history WHERE seriesId = :seriesId")
    suspend fun deleteSeriesHistory(seriesId: String)
}