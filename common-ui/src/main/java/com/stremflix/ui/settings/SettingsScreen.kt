package com.stremflix.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stremflix.core.domain.model.IdType
import com.stremflix.ui.R
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixRed
import com.stremflix.ui.theme.NetflixSurfaceLight
import com.stremflix.ui.theme.NetflixTextPrimary
import com.stremflix.ui.theme.NetflixTextSecondary
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTraktAuth: () -> Unit,
    isTvMode: Boolean = false,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsState()
    val isTraktConnected by viewModel.traktAuthState.collectAsState()

    // FIX: Use remember to hold local state for text fields
    var stremioBaseUrl by remember { mutableStateOf(prefs.stremioBaseUrl) }
    var tmdbApiKey by remember { mutableStateOf(prefs.tmdbApiKey) }
    var traktClientId by remember { mutableStateOf(prefs.traktClientId) }
    var traktClientSecret by remember { mutableStateOf(prefs.traktClientSecret) }
    var omdbApiKey by remember { mutableStateOf(prefs.omdbApiKey ?: "") }
    var tmdbLanguage by remember { mutableStateOf(prefs.tmdbLanguage) }
    var audioLang by remember { mutableStateOf(prefs.preferredAudioLanguage) }
    var subLang by remember { mutableStateOf(prefs.preferredSubtitleLanguage) }
    var forceSubs by remember { mutableStateOf(prefs.forceSubtitles) }

    // Update local state when prefs change
    LaunchedEffect(prefs) {
        stremioBaseUrl = prefs.stremioBaseUrl
        tmdbApiKey = prefs.tmdbApiKey
        traktClientId = prefs.traktClientId
        traktClientSecret = prefs.traktClientSecret
        omdbApiKey = prefs.omdbApiKey ?: ""
        tmdbLanguage = prefs.tmdbLanguage
        audioLang = prefs.preferredAudioLanguage
        subLang = prefs.preferredSubtitleLanguage
        forceSubs = prefs.forceSubtitles
    }

    Scaffold(
        containerColor = NetflixBlack,
        topBar = {
            Surface(color = NetflixSurfaceLight) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = NetflixTextPrimary
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.nav_settings),
                        style = MaterialTheme.typography.headlineMedium,
                        color = NetflixTextPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // TMDB
            item {
                Divider(color = Color(0xFF333333))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_language),
                    style = MaterialTheme.typography.headlineSmall,
                    color = NetflixTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                // TMDB Language
                LanguageDropdown(
                    label = stringResource(R.string.settings_tmdb_language),
                    selected = tmdbLanguage,
                    onSelected = {
                        tmdbLanguage = it
                        viewModel.updateTmdbLanguage(it) // Add this to ViewModel
                    }
                )

                Spacer(Modifier.height(12.dp))

                // Audio Language
                LanguageDropdown(
                    label = stringResource(R.string.settings_audio_pref),
                    selected = audioLang,
                    onSelected = {
                        audioLang = it
                        viewModel.updateAudioLanguage(it) // Add this to ViewModel
                    }
                )

                Spacer(Modifier.height(12.dp))

                // Subtitle Language
                LanguageDropdown(
                    label = stringResource(R.string.settings_sub_pref),
                    selected = subLang,
                    onSelected = {
                        subLang = it
                        viewModel.updateSubtitleLanguage(it) // Add this to ViewModel
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Force Subtitles Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.settings_force_subs), color = NetflixTextPrimary)
                        Text(text = stringResource(R.string.settings_force_subs_desc), style = MaterialTheme.typography.bodySmall, color = NetflixTextSecondary)
                    }
                    Switch(
                        checked = forceSubs,
                        onCheckedChange = {
                            forceSubs = it
                            viewModel.updateForceSubtitles(it) // Add this to ViewModel
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = NetflixRed, checkedTrackColor = NetflixRed.copy(alpha = 0.5f))
                    )
                }
                }
            // API Keys Section
            item {
                Text(
                    text = stringResource(id = R.string.settings_api_keys),
                    style = MaterialTheme.typography.headlineSmall,
                    color = NetflixTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                // FIX: Use local state with debounced save
                SettingsTextField(
                    label = stringResource(id = R.string.settings_stremio_base),
                    initialValue = stremioBaseUrl,
                    onSave = { newValue ->
                        stremioBaseUrl = newValue
                        viewModel.updateStremioBase(newValue)
                    }
                )

                SettingsTextField(
                    label = stringResource(id = R.string.settings_tmdb_key),
                    initialValue = tmdbApiKey,
                    onSave = { newValue ->
                        tmdbApiKey = newValue
                        viewModel.updateTmdbApiKey(newValue)
                    },
                    isPassword = true
                )

                SettingsTextField(
                    label = stringResource(id = R.string.settings_trakt_id),
                    initialValue = traktClientId,
                    onSave = { newValue ->
                        traktClientId = newValue
                        viewModel.updateTraktCredentials(newValue, traktClientSecret)
                    }
                )

                SettingsTextField(
                    label = stringResource(id = R.string.settings_trakt_secret),
                    initialValue = traktClientSecret,
                    onSave = { newValue ->
                        traktClientSecret = newValue
                        viewModel.updateTraktCredentials(traktClientId, newValue)
                    },
                    isPassword = true
                )

                SettingsTextField(
                    label = stringResource(id = R.string.settings_omdb_key),
                    initialValue = omdbApiKey,
                    onSave = { newValue ->
                        omdbApiKey = newValue
                        viewModel.updateOmdbConfig(newValue, prefs.omdbEnabled)
                    },
                    isPassword = true
                )
            }

            // Trakt Auth Section
            item {
                Divider(color = Color(0xFF333333))
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_trakt),
                        style = MaterialTheme.typography.headlineSmall,
                        color = NetflixTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))

                    if (isTraktConnected) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1B5E20), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_check_circle),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.settings_connected_trakt),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { viewModel.onLogoutTrakt() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = NetflixRed
                            )
                        ) {
                            Text(text = stringResource(R.string.settings_logout_trakt))
                        }
                    } else {
                        val canConnect = traktClientId.isNotEmpty() && traktClientSecret.isNotEmpty()

                        if (!canConnect) {
                            // Show warning
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF3E2723), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_error),
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.settings_trakt_required),
                                    color = NetflixTextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Button(
                            onClick = {
                                // Navigate to Trakt auth - this should work now
                                onNavigateToTraktAuth()
                            },
                            enabled = canConnect,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canConnect) NetflixRed else Color.Gray
                            )
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_link),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.settings_connect_trakt))
                        }
                    }
                }
            }

            // Preferences Section
            item {
                Divider(color = Color(0xFF333333))
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.headlineSmall,
                    color = NetflixTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                // Default ID Type Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Default ID Type", color = NetflixTextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (prefs.defaultIdType == IdType.IMDB) "IMDB" else "TMDB",
                            color = NetflixTextPrimary
                        )
                        Switch(
                            checked = prefs.defaultIdType == IdType.TMDB,
                            onCheckedChange = { checked ->
                                viewModel.updateDefaultIdType(if (checked) IdType.TMDB else IdType.IMDB)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NetflixRed,
                                checkedTrackColor = NetflixRed.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun SettingsTextField(
    label: String,
    initialValue: String,
    onSave: (String) -> Unit,
    isPassword: Boolean = false
) {
    var text by remember { mutableStateOf(initialValue) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(initialValue) {
        if (!isEditing) text = initialValue
    }

    LaunchedEffect(text, isEditing) {
        if (isEditing) {
            delay(600.milliseconds) // Wait 600ms after typing stops
            if (text != initialValue) {
                onSave(text)
            }
        }
    }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            isEditing = true
        },
        label = { Text(label, color = NetflixTextSecondary) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NetflixRed,
            unfocusedBorderColor = Color(0xFF333333),
            focusedTextColor = NetflixTextPrimary,
            unfocusedTextColor = NetflixTextPrimary,
            cursorColor = NetflixRed
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        textStyle = LocalTextStyle.current.copy(color = NetflixTextPrimary),
        singleLine = true
    )
}

@Composable
private fun LanguageDropdown(
    label: String,
    selected: String,
    onSelected: (String) -> Unit
) {
    val languages = listOf("en" to "English", "es" to "Spanish", "fr" to "French", "de" to "German", "ja" to "Japanese", "ko" to "Korean", "pt" to "Portuguese", "pt-BR" to "Brazillian", "ru" to "Russian", "it" to "Italian", "zh" to "Chinese")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = languages.find { it.first == selected }?.second ?: selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = NetflixTextSecondary) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NetflixRed,
                unfocusedBorderColor = Color(0xFF333333),
                focusedTextColor = NetflixTextPrimary,
                unfocusedTextColor = NetflixTextPrimary
            ),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(name, color = NetflixTextPrimary) },
                    onClick = {
                        onSelected(code)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(textColor = NetflixTextPrimary)
                )
            }
        }
    }
}