package com.stremflix.data.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.stremflix.core.model.PlaybackPreferences
import com.stremflix.core.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsStore(context: Context) : PreferencesRepository {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_settings",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val playbackFlow = MutableStateFlow(readPlayback())

    override fun observePlaybackPreferences(): Flow<PlaybackPreferences> = playbackFlow.asStateFlow()

    override suspend fun updatePlaybackPreferences(update: PlaybackPreferences) {
        prefs.edit()
            .putInt("skip_back", update.skipBackSeconds)
            .putBoolean("mute_on_start", update.muteOnStartup)
            .putBoolean("foreign_only", update.subtitle.foreignAudioOnly)
            .putBoolean("auto_trailer", update.autoTrailerPreview)
            .putInt("trailer_timeout", update.trailerPreviewTimeoutSeconds)
            .apply()
        playbackFlow.value = update
    }

    override suspend fun updateTmdbKey(key: String) = prefs.edit().putString("tmdb_key", key).apply()
    override suspend fun updateTraktKey(key: String) = prefs.edit().putString("trakt_key", key).apply()
    override suspend fun updateStremioManifestUrl(url: String) = prefs.edit().putString("stremio_manifest", url).apply()

    override suspend fun tmdbKey(): String? = prefs.getString("tmdb_key", null)
    override suspend fun traktKey(): String? = prefs.getString("trakt_key", null)
    override suspend fun stremioManifestUrl(): String? = prefs.getString("stremio_manifest", null)

    private fun readPlayback(): PlaybackPreferences = PlaybackPreferences(
        skipBackSeconds = prefs.getInt("skip_back", 10),
        muteOnStartup = prefs.getBoolean("mute_on_start", false),
        subtitle = com.stremflix.core.model.SubtitlePreferences(
            foreignAudioOnly = prefs.getBoolean("foreign_only", true)
        ),
        autoTrailerPreview = prefs.getBoolean("auto_trailer", true),
        trailerPreviewTimeoutSeconds = prefs.getInt("trailer_timeout", 5)
    )
}
