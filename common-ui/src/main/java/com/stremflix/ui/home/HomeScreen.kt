package com.stremflix.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stremflix.core.domain.model.ContentType
import com.stremflix.data.model.ContentItem
import com.stremflix.data.model.Stream
import com.stremflix.ui.R
import com.stremflix.ui.details.StreamSelectionDialog
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixTextPrimary
import com.stremflix.ui.theme.NetflixTextSecondary

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetails: (String, String?, String, String) -> Unit,
    onNavigateToPlayback: (String, String, String?, String, String) -> Unit,
    isTvMode: Boolean = false,
    filterType: String? = null,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val showStreamDialog by viewModel.showStreamDialog.collectAsState()
    val streams by viewModel.streams.collectAsState()

    if (showStreamDialog) {
        StreamSelectionDialog(
            streams = streams,
            onDismiss = { viewModel.dismissStreamDialog() },
            onStreamSelected = { stream ->
                viewModel.onStreamSelected(stream)
                // Get the current hero item (you'll need to track this in ViewModel)
                // For now, navigate with placeholder values
                if (stream != null) {
                    // You need to store the selected item in ViewModel
                    val item = viewModel.currentSelectedItem
                    if (item != null) {
                        onNavigateToPlayback(stream.url, item.title, item.synopsis, item.id, item.type.name.lowercase())
                    }
                }
            }
        )
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = when(filterType) {
                "tv" -> stringResource(R.string.nav_tv_shows)
                "movie" -> stringResource(R.string.nav_movies)
                "list" -> stringResource(R.string.nav_my_list)
                else -> stringResource(R.string.app_name)
            }, color = NetflixTextPrimary) },
            actions = {
                // Settings button in top bar
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = NetflixTextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = NetflixBlack,
                titleContentColor = NetflixTextPrimary
            )
        )
    }){
        padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)  // ADD THIS to prevent content cutting
                .background(NetflixBlack)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(
                        color = NetflixTextPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HomeUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_error),
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Error Loading Content",
                            style = MaterialTheme.typography.headlineMedium,
                            color = NetflixTextPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NetflixTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is HomeUiState.Success -> {
                    if (state.rows.isEmpty()) {
                        // Empty state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No content available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = NetflixTextSecondary
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            // Hero section
                            val firstRow = state.rows.firstOrNull()
                            if (firstRow != null && firstRow.items.isNotEmpty() && filterType == null) {
                                item {
                                    HeroCard(
                                        item = firstRow.items.first(),
                                        onPlayClick = { viewModel.onPlayClicked(firstRow.items.first()) },
                                        onMoreInfoClick = {
                                            onNavigateToDetails(
                                                firstRow.items.first().title,
                                                firstRow.items.first().synopsis,
                                                firstRow.items.first().id,
                                                firstRow.items.first().type.name.lowercase()
                                            )
                                        },
                                        modifier = Modifier.fillParentMaxWidth()
                                    )
                                    Spacer(Modifier.height(24.dp))
                                }
                            }

                            // Content rows
                            items(
                                items = state.rows,
                                key = { it.title }
                            ) { rowData ->
                                ContentRow(
                                    title = rowData.title,
                                    items = rowData.items,
                                    isLarge = rowData.isLarge,
                                    onItemSelected = { item ->
                                        onNavigateToDetails(item.title, item.synopsis, item.id, item.type.name.lowercase())
                                    }
                                )
                            }

                            item { Spacer(Modifier.height(100.dp)) }
                        }
                    }
                }
            }
        }
    }
}