package com.stremflix.data.repository

import com.stremflix.core.domain.model.IdType
import com.stremflix.core.domain.model.OmdbProvider
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val dataSource: PreferencesDataSource
) {

    suspend fun updateStremioBase(url: String) = dataSource.setStremioBaseUrl(url)
    suspend fun updateTmdbKey(key: String) = dataSource.setTmdbApiKey(key)
    suspend fun updateTraktCredentials(id: String, secret: String) {
        dataSource.setTraktClientId(id)
        dataSource.setTraktClientSecret(secret)
    }
    suspend fun updateOmdbConfig(key: String?, enabled: Boolean, providers: Set<OmdbProvider>) {
        key?.let { dataSource.setOmdbApiKey(it) }
        dataSource.setOmdbEnabled(enabled)
        dataSource.setOmdbProviders(providers)
    }
    suspend fun updateDefaultIdType(type: IdType) = dataSource.setDefaultIdType(type)
    suspend fun updatePlaybackThresholds(prefetch: Float, popup: Float) {
        dataSource.setPrefetchThreshold(prefetch)
        dataSource.setPopupThreshold(popup)
    }
}