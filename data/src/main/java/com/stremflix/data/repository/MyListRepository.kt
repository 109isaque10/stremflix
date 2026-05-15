package com.stremflix.data.repository

import com.stremflix.core.domain.model.ContentType
import com.stremflix.data.local.dao.MyListDao
import com.stremflix.data.local.entity.MyListEntity
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.mapper.toEntity
import com.stremflix.data.mapper.toDomainItem
import com.stremflix.data.model.ContentItem
import com.stremflix.data.remote.TraktApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.stremflix.core.domain.model.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyListRepository @Inject constructor(
    private val myListDao: MyListDao,
    private val traktApi: TraktApi,
    private val preferencesDataSource: PreferencesDataSource,
    private val contentRepository: ContentRepository
) {

    suspend fun getMyListItems(): Flow<List<ContentItem>> {
        return myListDao.getAllItems().map { entities ->
            entities.mapNotNull { entity ->
                val result = contentRepository.getDetails(
                    entity.tmdbId?.toString() ?: entity.id,
                    if (entity.type == "movie") ContentType.MOVIE else ContentType.SERIES
                )
                if (result is Result.Success) result.data else null
            }
        }
    }

    suspend fun addToMyList(item: ContentItem) {
        val entity = MyListEntity(
            id = item.externalIds.tmdbId?.toString() ?: item.id,
            type = if (item.type == ContentType.MOVIE) "movie" else "tv",
            title = item.title,
            posterUrl = item.posterUrl,
            year = item.year,
            dateAdded = System.currentTimeMillis(),
            traktId = item.externalIds.traktId,
            tmdbId = item.externalIds.tmdbId,
            imdbId = item.externalIds.imdbId
        )
        myListDao.addItem(entity)

        // Sync to Trakt if enabled
        if (isTraktEnabled()) {
            try {
                if (item.type == ContentType.MOVIE) {
                    traktApi.addToWatchlistMovie(item.externalIds.traktId ?: item.externalIds.tmdbId!!)
                } else {
                    traktApi.addToWatchlistShow(item.externalIds.traktId ?: item.externalIds.tmdbId!!)
                }
            } catch (e: Exception) {
                // Log error but don't fail local operation
                e.printStackTrace()
            }
        }
    }

    suspend fun removeFromMyList(itemId: String) {
        myListDao.removeItem(itemId)

        // Sync to Trakt if enabled
        if (isTraktEnabled()) {
            try {
                traktApi.removeFromWatchlist(itemId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun isInMyList(itemId: String): Boolean {
        return myListDao.isItemInList(itemId)
    }

    // Sync Trakt watchlist to local DB
    suspend fun syncFromTrakt() {
        if (!isTraktEnabled()) return

        try {
            val watchlist = traktApi.getWatchlist()

            // Clear local DB and repopulate from Trakt
            myListDao.clearAll()

            watchlist.forEach { item ->
                val entity = MyListEntity(
                    id = item.ids?.tmdb?.toString() ?: item.ids?.trakt.toString(),
                    type = item.type,
                    title = item.title ?: "",
                    posterUrl = null, // Will be fetched later
                    year = item.year,
                    dateAdded = System.currentTimeMillis(),
                    traktId = item.ids?.trakt,
                    tmdbId = item.ids?.tmdb,
                    imdbId = item.ids?.imdb
                )
                myListDao.addItem(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun isTraktEnabled(): Boolean {
        val prefs = preferencesDataSource.preferencesFlow.first()
        return !prefs.traktClientId.isNullOrBlank() &&
                !prefs.traktClientSecret.isNullOrBlank()
    }
}