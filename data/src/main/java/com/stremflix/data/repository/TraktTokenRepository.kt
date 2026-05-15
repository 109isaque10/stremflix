package com.stremflix.data.repository

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import com.stremflix.data.remote.dto.trakt.AccessTokenResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktTokenRepository @Inject constructor(
    private val encryptedPrefs: SharedPreferences
) {
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val KEY_ACCESS_TOKEN = "trakt_access_token"
        private const val KEY_REFRESH_TOKEN = "trakt_refresh_token"
        private const val KEY_EXPIRES_AT = "trakt_expires_at"
    }

    private val _tokenFlow = MutableStateFlow<AccessTokenResponse?>(loadTokens())
    val tokenFlow: StateFlow<AccessTokenResponse?> = _tokenFlow.asStateFlow()

    private fun loadTokens(): AccessTokenResponse? {
        val accessToken = encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
        val refreshToken = encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
        val expiresAt = encryptedPrefs.getLong(KEY_EXPIRES_AT, -1)

        return if (accessToken != null && refreshToken != null && expiresAt != -1L) {
            AccessTokenResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = 0, // We calculate expiry dynamically
                scope = null
            )
        } else {
            null
        }
    }

    suspend fun saveTokens(response: AccessTokenResponse, serverExpirySeconds: Int) {
        val expiresAt = Clock.System.now().toEpochMilliseconds() + (serverExpirySeconds * 1000)

        encryptedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, response.accessToken)
            .putString(KEY_REFRESH_TOKEN, response.refreshToken)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()

        _tokenFlow.update { response }
    }

    suspend fun clearTokens() {
        encryptedPrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_EXPIRES_AT)
            .apply()
        _tokenFlow.update { null }
    }

    fun isTokenExpired(): Boolean {
        val expiresAt = encryptedPrefs.getLong(KEY_EXPIRES_AT, -1)
        if (expiresAt == -1L) return true

        val now = Clock.System.now().toEpochMilliseconds()
        // Add buffer of 5 minutes for safety
        return now >= (expiresAt - 300000)
    }

    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }
}