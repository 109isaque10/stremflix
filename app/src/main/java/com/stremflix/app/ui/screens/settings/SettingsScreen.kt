package com.stremflix.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stremflix.commonui.theme.Black
import com.stremflix.commonui.theme.GrayText
import com.stremflix.commonui.theme.NetflixRed
import com.stremflix.commonui.theme.White

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Black).padding(16.dp)
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineMedium, color = White, modifier = Modifier.padding(bottom = 16.dp))
            
            Divider(color = GrayText.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 16.dp))
            
            // API Keys Section
            Text("API Configuration", style = MaterialTheme.typography.titleMedium, color = NetflixRed, modifier = Modifier.padding(bottom = 8.dp))
            
            OutlinedTextField(
                value = uiState.tmdbApiKey,
                onValueChange = { viewModel.updateTmdbKey(it) },
                label = { Text("TMDB API Key") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFF222), focusedContainerColor = Color(0xFF333))
            )
            
            OutlinedTextField(
                value = uiState.traktApiKey,
                onValueChange = { viewModel.updateTraktKey(it) },
                label = { Text("Trakt API Key") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFF222), focusedContainerColor = Color(0xFF333))            )
            
            // Stremio Section
            Text("Streaming", style = MaterialTheme.typography.titleMedium, color = NetflixRed, modifier = Modifier.padding(bottom = 8.dp))
            
            OutlinedTextField(
                value = uiState.stremioManifestUrl,
                onValueChange = { viewModel.updateStremioUrl(it) },
                label = { Text("Stremio Manifest URL") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFF222), focusedContainerColor = Color(0xFF333))
            )
            
            // Playback Section
            Text("Playback Preferences", style = MaterialTheme.typography.titleMedium, color = NetflixRed, modifier = Modifier.padding(bottom = 8.dp))
            
            // Subtitle Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                    .padding(16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Sample Subtitle Text (Style Preview)",
                    color = uiState.subtitleColor,
                    fontSize = uiState.subtitleSize,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // Subtitle Size Slider
            Text("Subtitle Size", color = White, modifier = Modifier.padding(bottom = 4.dp))
            Slider(
                value = uiState.subtitleSize.value,
                onValueChange = { viewModel.updateSubtitleSize(it) },
                valueRange = 12f..24f,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
            
            // Skip Duration
            Text("Skip Back Duration: ${uiState.skipBackDuration}s", color = White, modifier = Modifier.padding(bottom = 4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                listOf(5, 10, 15, 30).forEach { duration ->
                    FilterChip(
                        selected = uiState.skipBackDuration == duration,
                        onClick = { viewModel.updateSkipDuration(duration) },
                        label = { Text("$duration") },                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NetflixRed,
                            selectedLabelColor = White,
                            containerColor = Color(0xFF333),
                            labelColor = White
                        )
                    )
                }
            }
            
            // Toggles
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Mute on Startup", color = White)
                Switch(
                    checked = uiState.muteOnStartup,
                    onCheckedChange = { viewModel.toggleMuteOnStartup() },
                    colors = SwitchDefaults.colors(checkedThumbColor = NetflixRed, checkedTrackColor = NetflixRed.copy(alpha = 0.5f))
                )
            }
        }
    }
}