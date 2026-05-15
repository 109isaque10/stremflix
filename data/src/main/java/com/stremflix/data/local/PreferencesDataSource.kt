package com.stremflix.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.stremflix.core.domain.model.IdType
import com.stremflix.core.domain.model.OmdbProvider
import com.stremflix.core.util.ApiEndpoints
import com.stremflix.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // Keys
    private val KEY_STREMIO_BASE = stringPreferencesKey("stremio_base")
    private val KEY_TMDB_API_KEY = stringPreferencesKey("tmdb_api_key")
    private val KEY_TRAKT_CLIENT_ID = stringPreferencesKey("trakt_client_id")
    private val KEY_TRAKT_CLIENT_SECRET = stringPreferencesKey("trakt_client_secret")
    private val KEY_OMDB_API_KEY = stringPreferencesKey("omdb_api_key")
    private val KEY_OMDB_ENABLED = booleanPreferencesKey("omdb_enabled")
    private val KEY_OMDB_PROVIDERS = stringSetPreferencesKey("omdb_providers")
    private val KEY_DEFAULT_ID_TYPE = stringPreferencesKey("default_id_type")
    private val KEY_PREFETCH_THRESHOLD = floatPreferencesKey("prefetch_threshold")
    private val KEY_POPUP_THRESHOLD = floatPreferencesKey("popup_threshold")
    private val KEY_RECENT_SEARCHES = stringPreferencesKey("recent_searches_csv")
    private val KEY_TMDB_REGION = stringPreferencesKey("tmdb_region")
    private val KEY_TMDB_LANG = stringPreferencesKey("tmdb_lang")
    private val KEY_AUDIO_LANG = stringPreferencesKey("audio_lang")
    private val KEY_SUB_LANG = stringPreferencesKey("sub_lang")
    private val KEY_FORCE_SUBS = booleanPreferencesKey("force_subs")

    // Flows
    val stremioBaseUrl: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_STREMIO_BASE] ?: ApiEndpoints.STREMIO_BASE
    }

    val tmdbApiKey: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_TMDB_API_KEY] ?: "" // No hardcoded fallback for keys, requires user input
    }

    val traktClientId: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_TRAKT_CLIENT_ID] ?: ""
    }

    val traktClientSecret: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_TRAKT_CLIENT_SECRET] ?: ""
    }

    val omdbApiKey: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_OMDB_API_KEY] // Returns null if not configured
    }

    val omdbEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_OMDB_ENABLED] ?: false
    }

    val omdbProviders: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[KEY_OMDB_PROVIDERS] ?: emptySet()
    }

    val defaultIdType: Flow<IdType> = dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_ID_TYPE]?.let { IdType.valueOf(it) } ?: IdType.IMDB
    }

    val prefetchThreshold: Flow<Float> = dataStore.data.map { prefs ->
        prefs[KEY_PREFETCH_THRESHOLD] ?: 0.94f
    }

    val popupThreshold: Flow<Float> = dataStore.data.map { prefs ->
        prefs[KEY_POPUP_THRESHOLD] ?: 0.97f
    }

    val tmdbLanguage: Flow<String> = dataStore.data.map { prefs -> prefs[KEY_TMDB_LANG] ?: "en-US" }
    val preferredAudioLanguage: Flow<String> = dataStore.data.map { prefs -> prefs[KEY_AUDIO_LANG] ?: "en" }
    val preferredSubtitleLanguage: Flow<String> = dataStore.data.map { prefs -> prefs[KEY_SUB_LANG] ?: "en" }
    val forceSubtitles: Flow<Boolean> = dataStore.data.map { prefs -> prefs[KEY_FORCE_SUBS] ?: false }

    // Update functions
    suspend fun setStremioBaseUrl(url: String) {
        dataStore.edit { it[KEY_STREMIO_BASE] = url }
    }

    suspend fun setTmdbApiKey(key: String) {
        dataStore.edit { it[KEY_TMDB_API_KEY] = key }
    }

    suspend fun setTraktClientId(clientId: String) {
        dataStore.edit { it[KEY_TRAKT_CLIENT_ID] = clientId }
    }

    suspend fun setTraktClientSecret(secret: String) {
        dataStore.edit { it[KEY_TRAKT_CLIENT_SECRET] = secret }
    }

    suspend fun setOmdbApiKey(key: String) {
        dataStore.edit { it[KEY_OMDB_API_KEY] = key }
    }

    suspend fun setOmdbEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_OMDB_ENABLED] = enabled }
    }

    suspend fun setOmdbProviders(providers: Set<OmdbProvider>) {
        dataStore.edit { it[KEY_OMDB_PROVIDERS] = providers.map { p -> p.name }.toSet() }
    }

    suspend fun setDefaultIdType(type: IdType) {
        dataStore.edit { it[KEY_DEFAULT_ID_TYPE] = type.name }
    }

    suspend fun setPrefetchThreshold(threshold: Float) {
        dataStore.edit { it[KEY_PREFETCH_THRESHOLD] = threshold }
    }

    suspend fun setPopupThreshold(threshold: Float) {
        dataStore.edit { it[KEY_POPUP_THRESHOLD] = threshold }
    }

    val tmdbRegion: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_TMDB_REGION] ?: "US" // Default to US
    }

    suspend fun setTmdbRegion(region: String) {
        dataStore.edit { it[KEY_TMDB_REGION] = region }
    }

    suspend fun setTmdbLanguage(lang: String) { dataStore.edit { it[KEY_TMDB_LANG] = lang } }
    suspend fun setPreferredAudioLanguage(lang: String) { dataStore.edit { it[KEY_AUDIO_LANG] = lang } }
    suspend fun setPreferredSubtitleLanguage(lang: String) { dataStore.edit { it[KEY_SUB_LANG] = lang } }
    suspend fun setForceSubtitles(force: Boolean) { dataStore.edit { it[KEY_FORCE_SUBS] = force } }

    val recentSearches: Flow<List<String>> = dataStore.data.map { prefs ->
        prefs[KEY_RECENT_SEARCHES]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun addRecentSearch(query: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_RECENT_SEARCHES]?.split(",")?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
            current.remove(query) // Remove existing to move to front
            current.add(0, query)
            if (current.size > 10) current.removeAt(current.size - 1)
            prefs[KEY_RECENT_SEARCHES] = current.joinToString(",")
        }
    }

    suspend fun clearRecentSearches() {
        dataStore.edit { it.remove(KEY_RECENT_SEARCHES) }
    }

    val preferencesFlow: Flow<UserPreferences> = combine(
        stremioBaseUrl, tmdbApiKey, traktClientId, traktClientSecret,
        omdbApiKey, omdbEnabled, omdbProviders, defaultIdType,
        prefetchThreshold, popupThreshold, tmdbRegion,
        tmdbLanguage, preferredAudioLanguage, preferredSubtitleLanguage, forceSubtitles
    ) { args ->
        UserPreferences(
            stremioBaseUrl = args[0] as String,
            tmdbApiKey = args[1] as String,
            traktClientId = args[2] as String,
            traktClientSecret = args[3] as String,
            omdbApiKey = args[4] as String?,
            omdbEnabled = args[5] as Boolean,
            omdbProviders = (args[6] as Set<String>).map { OmdbProvider.valueOf(it) }.toSet(),
            defaultIdType = args[7] as IdType,
            prefetchThreshold = args[8] as Float,
            popupThreshold = args[9] as Float,
            tmdbRegion = args[10] as String,
            tmdbLanguage = args[11] as String,
            preferredAudioLanguage = args[12] as String,
            preferredSubtitleLanguage = args[13] as String,
            forceSubtitles = args[14] as Boolean
        )
    }

}