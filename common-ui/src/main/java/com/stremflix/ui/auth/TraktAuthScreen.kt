// common-ui/src/main/java/com/stremflix/ui/auth/TraktAuthScreen.kt

package com.stremflix.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stremflix.ui.R
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixRed
import com.stremflix.ui.theme.NetflixSurfaceLight
import com.stremflix.ui.theme.NetflixTextPrimary
import com.stremflix.ui.theme.NetflixTextSecondary
import kotlinx.coroutines.launch

@Composable
fun TraktAuthScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: TraktAuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val deviceCode by viewModel.deviceCode.collectAsState()
    val verificationUrl by viewModel.verificationUrl.collectAsState()

    // Handle events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is TraktAuthEvent.Error -> {
                    snackbarHostState.showSnackbar("Error: ${event.message}")
                }
                is TraktAuthEvent.LoginSuccess -> {
                    onSuccess()
                }
                is TraktAuthEvent.OpenBrowser -> {
                    // Open browser for OAuth
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(event.url)
                    )
                    context.startActivity(intent)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = NetflixBlack,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_connect_trakt), color = NetflixTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = NetflixTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NetflixBlack
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (val state = uiState) {
                is TraktAuthUiState.Idle -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(32.dp)) {
                        Icon(ImageVector.vectorResource(R.drawable.ic_link), contentDescription = null, tint = NetflixRed, modifier = Modifier.size(64.dp))
                        Text(text = stringResource(R.string.settings_connect_trakt), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = NetflixTextPrimary)
                        Text(text = stringResource(R.string.trakt_about), style = MaterialTheme.typography.bodyMedium, color = NetflixTextSecondary, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.startMobileAuth() }, colors = ButtonDefaults.buttonColors(containerColor = NetflixRed), modifier = Modifier.fillMaxWidth()) {
                            Text(text = stringResource(R.string.trakt_browser_auth))
                        }
                        OutlinedButton(onClick = { viewModel.startTvAuth() }, colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent, contentColor = NetflixTextPrimary), modifier = Modifier.fillMaxWidth()) {
                            Text(text = stringResource(R.string.trakt_tv_code_auth))
                        }
                    }
                }
                is TraktAuthUiState.DeviceCodeLoading -> {
                    CircularProgressIndicator(color = NetflixRed)
                    Spacer(Modifier.height(16.dp))
                    Text(text = stringResource(R.string.trakt_get_code), color = NetflixTextSecondary)
                }
                is TraktAuthUiState.ShowDeviceCode -> {
                    deviceCode?.let { code ->
                        println("✅ Showing device code: ${code.userCode}")
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.trakt_activate),
                                style = MaterialTheme.typography.headlineSmall,
                                color = NetflixTextPrimary
                            )

                            Card(
                                colors = CardDefaults.cardColors(containerColor = NetflixSurfaceLight),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = verificationUrl,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = NetflixRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        text = code.userCode,
                                        style = MaterialTheme.typography.displayLarge,
                                        color = NetflixTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Text(
                                text = stringResource(R.string.trakt_enter_code_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = NetflixTextSecondary
                            )

                            if (state is TraktAuthUiState.PollingDeviceCode) {
                                Spacer(Modifier.height(16.dp))
                                CircularProgressIndicator(color = NetflixRed)
                                Text(
                                    text = stringResource(R.string.trakt_waiting),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NetflixTextSecondary
                                )
                            }
                        }
                    } ?: run {
                        // Fallback if deviceCode is null
                        Text("Error: No device code received", color = Color.Red)
                    }
                }
                is TraktAuthUiState.PollingDeviceCode -> {
                    CircularProgressIndicator(color = NetflixRed)
                    Spacer(Modifier.height(16.dp))
                    Text(text = stringResource(R.string.trakt_waiting), style = MaterialTheme.typography.bodyMedium, color = NetflixTextSecondary)
                }
                is TraktAuthUiState.Loading -> {
                    CircularProgressIndicator(color = NetflixRed)
                }
            }
        }
    }
}