// common-ui/src/main/java/com/stremflix/ui/auth/TraktAuthViewModel.kt

package com.stremflix.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.manager.TraktOAuthManager
import com.stremflix.data.remote.dto.trakt.DeviceCodeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

sealed class TraktAuthUiState {
    object Idle : TraktAuthUiState()
    object DeviceCodeLoading : TraktAuthUiState()
    data class ShowDeviceCode(val code: DeviceCodeResponse) : TraktAuthUiState()
//    object PollingDeviceCode : TraktAuthUiState()
    object Loading : TraktAuthUiState()
}

sealed class TraktAuthEvent {
    data class Error(val message: String) : TraktAuthEvent()
    object LoginSuccess : TraktAuthEvent()
    data class OpenBrowser(val url: String) : TraktAuthEvent()
}

@HiltViewModel
class TraktAuthViewModel @Inject constructor(
    private val oAuthManager: TraktOAuthManager,
    private val preferencesDataSource: PreferencesDataSource,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _uiState = MutableStateFlow<TraktAuthUiState>(TraktAuthUiState.Idle)
    val uiState: StateFlow<TraktAuthUiState> = _uiState.asStateFlow()

    private val _deviceCode = MutableStateFlow<DeviceCodeResponse?>(null)
    val deviceCode: StateFlow<DeviceCodeResponse?> = _deviceCode.asStateFlow()

    private val _isPolling = MutableStateFlow(false)
    val isPolling: StateFlow<Boolean> = _isPolling.asStateFlow()

    private val _verificationUrl = MutableStateFlow("")
    val verificationUrl: StateFlow<String> = _verificationUrl.asStateFlow()

    private val _eventChannel = Channel<TraktAuthEvent>(Channel.BUFFERED)
    val eventFlow = _eventChannel.receiveAsFlow()

    private val _authSuccess = MutableStateFlow(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    fun startMobileAuth() {
        viewModelScope.launch {
            try {
                val prefs = preferencesDataSource.preferencesFlow.first()
                val clientId = prefs.traktClientId

                if (clientId.isEmpty()) {
                    _eventChannel.send(TraktAuthEvent.Error("Client ID not configured in Settings"))
                    return@launch
                }

                val authUrl = oAuthManager.getAuthorizationUrl(clientId, URLEncoder.encode("com.stremflix://trakt/callback", StandardCharsets.UTF_8.toString()))
                _eventChannel.send(TraktAuthEvent.OpenBrowser(authUrl))

                // Note: After browser opens, user needs to authorize,
                // then we need to handle the callback. This requires
                // implementing the deep link handler.

            } catch (e: Exception) {
                _eventChannel.send(TraktAuthEvent.Error(e.message ?: "Auth failed"))
            }
        }
    }

    fun startTvAuth() {
        _uiState.value = TraktAuthUiState.DeviceCodeLoading

        viewModelScope.launch(dispatchers.io) {
            try {
                val prefs = preferencesDataSource.preferencesFlow.first()
                val clientId = prefs.traktClientId

                if (clientId.isEmpty()) {
                    _eventChannel.send(TraktAuthEvent.Error("Client ID not configured in Settings"))
                    _uiState.value = TraktAuthUiState.Idle
                    return@launch
                }

                val response = oAuthManager.requestDeviceCode()

                // STORE THE DEVICE CODE
                _deviceCode.value = response
                _verificationUrl.value = response.verificationUrl

                // TRANSITION TO SHOW DEVICE CODE STATE
                _uiState.value = TraktAuthUiState.ShowDeviceCode(response)

                // START POLLING AFTER SHOWING THE CODE
                pollForTokenWithTimeout(response.deviceCode, response.interval)

            } catch (e: Exception) {
                e.printStackTrace()
                _eventChannel.send(TraktAuthEvent.Error(e.message ?: "Failed to get device code"))
                _uiState.value = TraktAuthUiState.Idle
            }
        }
    }

    private suspend fun pollForTokenWithTimeout(deviceCode: String, intervalSeconds: Int) {
//        _uiState.value = TraktAuthUiState.PollingDeviceCode
        _isPolling.value = true
        val maxAttempts = (600 / intervalSeconds) // 10 minutes max
        var attempts = 0

        while (attempts < maxAttempts) {
            delay((intervalSeconds * 1000).toLong())
            attempts++


            try {
                val result = oAuthManager.pollForDeviceToken(deviceCode)
                if (result != null) {
                    _eventChannel.send(TraktAuthEvent.LoginSuccess)
                    _isPolling.value = false
                    return
                }
            } catch (e: Exception) {
                // Trakt returns error if not yet authorized - this is normal
            }
        }

        _eventChannel.send(TraktAuthEvent.Error("Authorization timed out. Please try again."))
        _uiState.value = TraktAuthUiState.Idle
    }

    fun handleOAuthCallback(code: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val prefs = preferencesDataSource.preferencesFlow.first()
                val clientId = prefs.traktClientId
                val clientSecret = prefs.traktClientSecret

                if (clientId.isEmpty() || clientSecret.isEmpty()) {
                    _eventChannel.send(TraktAuthEvent.Error("Client credentials not configured"))
                    return@launch
                }

                val redirectUri = "com.stremflix://trakt/callback"

                val response = oAuthManager.exchangeCode(
                    code = code,
                    clientId = clientId,
                    clientSecret = clientSecret,
                    redirectUri = redirectUri
                )

                _eventChannel.send(TraktAuthEvent.LoginSuccess)
            } catch (e: Exception) {
                e.printStackTrace()
                _eventChannel.send(TraktAuthEvent.Error("Failed to exchange code: ${e.message}"))
            }
        }
    }
}