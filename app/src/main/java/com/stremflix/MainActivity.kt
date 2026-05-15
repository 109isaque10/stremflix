package com.stremflix

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.stremflix.ui.StremFlixApp
import com.stremflix.ui.auth.TraktAuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val traktAuthViewModel: TraktAuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle OAuth callback if present in intent
        handleOAuthCallback(intent)

        setContent {
            StremFlixApp(isTvMode = false)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle OAuth callback when app is already running
        handleOAuthCallback(intent)
    }

    private fun handleOAuthCallback(intent: Intent?) {
        val uri = intent?.data

        if (uri != null) {

            // Check if this is our OAuth callback
            if ((uri.scheme == "com.stremflix" || uri.scheme == "com.stremflix.debug" ) &&
                uri.host == "trakt" &&
                uri.path == "/callback") {

                val code = uri.getQueryParameter("code")

                if (code != null) {
                    // Exchange the code for tokens
                    lifecycleScope.launch {
                        try {
                            // Get credentials from preferences
                            // This requires accessing PreferencesRepository
                            // For now, we'll emit an event to the ViewModel
                            traktAuthViewModel.handleOAuthCallback(code)
                        } catch (e: Exception) {
                            println("❌ [MainActivity] Error handling OAuth: ${e.message}")
                        }
                    }
                }
            }
        }
    }
}