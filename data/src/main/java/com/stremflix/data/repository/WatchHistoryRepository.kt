// data/src/main/java/com/stremflix/data/repository/WatchHistoryRepository.kt

package com.stremflix.data.repository

import com.stremflix.data.local.dao.WatchHistoryDao
import com.stremflix.data.local.entity.WatchHistoryEntity
import com.stremflix.data.mapper.toDomain
import com.stremflix.data.mapper.toEntity
import com.stremflix.data.model.WatchHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchHistoryRepository @Inject constructor(
    private val watchHistoryDao: WatchHistoryDao
) {

    fun getAllWatchHistory(): Flow<List<WatchHistory>> {
        return watchHistoryDao.getAllHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getLastWatchedEpisode(seriesId: String): WatchHistory? {
        return watchHistoryDao.getLastWatchedForSeries(seriesId)?.toDomain()
    }

    suspend fun getWatchHistory(seriesId: String): List<WatchHistory> {
        return watchHistoryDao.getHistoryForSeries(seriesId).map { it.toDomain() }
    }

    suspend fun saveWatchHistory(history: WatchHistory) {
        watchHistoryDao.insert(history.toEntity())
    }

    suspend fun updateWatchProgress(
        seriesId: String,
        seasonNumber: Int,
        episodeNumber: Int,
        progress: Float
    ) {
        val existing = watchHistoryDao.getEpisode(seriesId, seasonNumber, episodeNumber)

        val updated = existing?.copy(
            watchProgress = progress,
            lastWatchedAt = System.currentTimeMillis()
        ) ?: WatchHistoryEntity(
            seriesId = seriesId,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            episodeId = "${seriesId}_S${seasonNumber}E${episodeNumber}",
            watched = progress >= 0.95f,
            watchProgress = progress,
            lastWatchedAt = System.currentTimeMillis(),
            title = "",
            synopsis = null
        )

        watchHistoryDao.insert(updated)
    }

    suspend fun syncHistoryFromTrakt(history: List<WatchHistory>) {
        val entities = history.map { item ->
            WatchHistoryEntity(
                episodeId = item.episodeId,
                seriesId = item.seriesId,
                seasonNumber = item.seasonNumber,
                episodeNumber = item.episodeNumber,
                watched = item.watched,
                watchProgress = item.watchProgress,
                lastWatchedAt = item.lastWatchedAt,
                title = item.title,
                synopsis = item.synopsis
            )
        }
        if (entities.isNotEmpty()) {
            watchHistoryDao.insertAll(entities)
        }
    }

    suspend fun markAsWatched(
        seriesId: String,
        seasonNumber: Int,
        episodeNumber: Int
    ) {
        val existing = watchHistoryDao.getEpisode(seriesId, seasonNumber, episodeNumber)

        existing?.let {
            watchHistoryDao.insert(it.copy(watched = true, watchProgress = 1.0f))
        }
    }
}