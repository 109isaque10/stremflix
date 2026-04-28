package com.stremflix.data.repository

import com.stremflix.core.model.MediaItem
import com.stremflix.core.repository.HomeRepository
import com.stremflix.data.db.MediaDao
import com.stremflix.data.remote.TmdbApi
import com.stremflix.data.mapper.toMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val mediaDao: MediaDao
) : HomeRepository {
    
    override fun getHomeContent(): Flow<List<MediaItem>> = flow {
        val currentTime = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        
        // Try Cache First
        val cachedItems = mediaDao.getAllMedia()
        if (cachedItems.isNotEmpty() && (currentTime - cachedItems.first().lastUpdated) < oneDayMs) {
            emit(cachedItems)
            return@flow
        }
        
        // Fetch from Network
        val response = tmdbApi.getTrending("all", "week")
        val items = response.results.map { it.toMediaItem().copy(lastUpdated = currentTime) }
        
        // Update Cache
        mediaDao.insertAll(items)
        
        emit(items)
    }.flowOn(Dispatchers.IO)
}