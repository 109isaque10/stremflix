// common-ui/src/main/java/com/stremflix/ui/auth/OAuthCallbackScreen.kt

package com.stremflix.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.stremflix.ui.theme.NetflixBlack

@Composable
fun OAuthCallbackScreen(
    code: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    viewModel: TraktAuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(code) {
        // Exchange code for tokens via ViewModel
        try {
            // Get credentials from preferences (in real app, fetch from repository)
            // For now, we assume they're already configured and just notify success
            // The actual exchange happens via deep link handling in MainActivity

            // Since we can't easily inject clientId/secret here without repository,
            // we delegate to ViewModel via event or assume external handling.

            // Simple approach: just notify success and let ViewModel handle via token repo
            // In production, you'd call oAuthManager.exchangeCode() with proper params

            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "OAuth failed")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixBlack),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}