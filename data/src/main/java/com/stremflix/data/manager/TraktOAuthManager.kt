package com.stremflix.data.manager

import android.content.Context
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.remote.TraktApi
import com.stremflix.data.remote.dto.trakt.AccessTokenResponse
import com.stremflix.data.remote.dto.trakt.DeviceCodeResponse
import com.stremflix.data.repository.TraktTokenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktOAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: TraktApi,
    private val preferencesDataSource: PreferencesDataSource,
    private val tokenRepository: TraktTokenRepository,
    private val dispatchers: AppDispatchers
) {
    fun getAuthorizationUrl(clientId: String, redirectUri: String): String {
        return "https://trakt.tv/oauth/authorize?" +
                "response_type=code&" +
                "client_id=$clientId&" +
                "redirect_uri=$redirectUri&"
    }

    suspend fun exchangeCode(
        code: String,
        clientId: String,
        clientSecret: String,
        redirectUri: String
    ): AccessTokenResponse {
        val response = api.exchangeCode(code, clientId, clientSecret, redirectUri)
        tokenRepository.saveTokens(response, response.expiresIn)
        return response
    }

    suspend fun getAccessTokenForRequest(): String? {
        val current = tokenRepository.tokenFlow.value ?: return null
        if (tokenRepository.isTokenExpired()) {
            refreshToken()
        }
        return tokenRepository.tokenFlow.value?.accessToken
    }

    private suspend fun refreshToken() {
        val refreshToken = tokenRepository.getRefreshToken() ?: return
        val clientId = preferencesDataSource.traktClientId.first()
        val clientSecret = preferencesDataSource.traktClientSecret.first()

        val newTokens = api.refreshToken(refreshToken, clientId, clientSecret)
        tokenRepository.saveTokens(newTokens, newTokens.expiresIn)
    }

    suspend fun revokeToken() {
        val token = tokenRepository.tokenFlow.value?.accessToken ?: return
        val clientId = preferencesDataSource.traktClientId.first()
        val clientSecret = preferencesDataSource.traktClientSecret.first()

        api.revokeToken(token, clientId, clientSecret)
        tokenRepository.clearTokens()
    }

    // TV Device Code Flow
    suspend fun requestDeviceCode(): DeviceCodeResponse {
        val clientId = preferencesDataSource.traktClientId.first()
        return api.getDeviceCode(clientId)
    }

    suspend fun pollForDeviceToken(deviceCode: String): AccessTokenResponse? {
        val clientId = preferencesDataSource.traktClientId.first()
        val clientSecret = preferencesDataSource.traktClientSecret.first()

        return try {
            val response = api.pollAccessToken(deviceCode, clientId, clientSecret)
            tokenRepository.saveTokens(response, response.expiresIn)
            response
        } catch (e: Exception) {
            null
        }
    }

    fun isAuthenticated(): Boolean {
        val token = tokenRepository.tokenFlow.value
        return token != null && !tokenRepository.isTokenExpired()
    }
}