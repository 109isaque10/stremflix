package com.stremflix.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremflix.core.domain.model.IdType
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.manager.TraktOAuthManager
import com.stremflix.data.model.UserPreferences
import com.stremflix.data.repository.TraktTokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource,
    private val tokenRepository: TraktTokenRepository,
    private val oAuthManager: TraktOAuthManager,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesDataSource.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences(
            stremioBaseUrl = "", tmdbApiKey = "", traktClientId = "", traktClientSecret = "",
            omdbApiKey = null, omdbEnabled = false, omdbProviders = emptySet(),
            defaultIdType = IdType.IMDB, prefetchThreshold = 0.94f, popupThreshold = 0.97f,
            tmdbRegion = "US",
        ))

    val traktAuthState: StateFlow<Boolean> = tokenRepository.tokenFlow
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun updateStremioBase(url: String) {
        viewModelScope.launch(dispatchers.io) {
            delay(500L.milliseconds)
            preferencesDataSource.setStremioBaseUrl(url)
        }
    }

    fun updateTmdbApiKey(key: String) {
        viewModelScope.launch(dispatchers.io) {
            delay(500L.milliseconds)
            preferencesDataSource.setTmdbApiKey(key)
        }
    }

    fun updateTmdbRegion(region: String) {
        viewModelScope.launch(dispatchers.io) {
            delay(500L.milliseconds)
            preferencesDataSource.setTmdbRegion(region)
        }
    }

    fun updateTraktCredentials(clientId: String, clientSecret: String) {
        viewModelScope.launch(dispatchers.io) {
            delay(500L.milliseconds)
            preferencesDataSource.setTraktClientId(clientId)
            preferencesDataSource.setTraktClientSecret(clientSecret)
        }
    }

    fun updateOmdbConfig(apiKey: String?, enabled: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            delay(500L.milliseconds)
            if (apiKey != null) preferencesDataSource.setOmdbApiKey(apiKey)
            preferencesDataSource.setOmdbEnabled(enabled)
        }
    }

    fun updateDefaultIdType(type: IdType) {
        viewModelScope.launch(dispatchers.io) {
            preferencesDataSource.setDefaultIdType(type)
        }
    }

    fun updateTmdbLanguage(lang: String) {
        viewModelScope.launch(dispatchers.io) { preferencesDataSource.setTmdbLanguage(lang) }
    }

    fun updateAudioLanguage(lang: String) {
        viewModelScope.launch(dispatchers.io) { preferencesDataSource.setPreferredAudioLanguage(lang) }
    }

    fun updateSubtitleLanguage(lang: String) {
        viewModelScope.launch(dispatchers.io) { preferencesDataSource.setPreferredSubtitleLanguage(lang) }
    }

    fun updateForceSubtitles(force: Boolean) {
        viewModelScope.launch(dispatchers.io) { preferencesDataSource.setForceSubtitles(force) }
    }

    fun onLogoutTrakt() {
        viewModelScope.launch(dispatchers.io) {
            logoutTrakt()
        }
    }

    suspend fun logoutTrakt() {
        oAuthManager.revokeToken()
    }
}