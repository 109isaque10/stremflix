package com.stremflix.ui.search

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stremflix.data.model.ContentItem
import com.stremflix.ui.components.ContentCard
import com.stremflix.ui.components.ErrorState
import com.stremflix.ui.R
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixRed
import com.stremflix.ui.theme.NetflixSurfaceLight
import com.stremflix.ui.theme.NetflixTextPrimary
import com.stremflix.ui.theme.NetflixTextSecondary

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateToDetails: (String, String?, String, String) -> Unit,
    isTvMode: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (isTvMode) focusRequester.requestFocus()
    }

    Scaffold(
        containerColor = NetflixBlack,
        topBar = {
            Surface(color = NetflixSurfaceLight) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_search),
                        contentDescription = "Search",
                        tint = NetflixTextSecondary
                    )
                    OutlinedTextField(
                        value = query,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.search_hint),
                                color = NetflixTextSecondary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NetflixRed,
                            unfocusedBorderColor = NetflixTextSecondary.copy(alpha = 0.3f),
                            cursorColor = NetflixRed,
                            focusedTextColor = NetflixTextPrimary,
                            unfocusedTextColor = NetflixTextPrimary
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(4.dp)
                    )
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_clear),
                                contentDescription = "Clear",
                                tint = NetflixTextSecondary
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.search_recent),
                                style = MaterialTheme.typography.headlineSmall,
                                color = NetflixTextPrimary
                            )
                            if (recentSearches.isNotEmpty()) {
                                TextButton(onClick = { viewModel.clearRecent() }) {
                                    Text(
                                        text = stringResource(id = R.string.search_clear_recent),
                                        color = NetflixTextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (recentSearches.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(top = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.search_idle_prompt),
                                    color = NetflixTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            recentSearches.forEach { search ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.onQueryChange(search) }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_history),
                                        contentDescription = null,
                                        tint = NetflixTextSecondary
                                    )
                                    Text(
                                        text = search,
                                        color = NetflixTextSecondary,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
                is SearchUiState.Loading -> {
                    CircularProgressIndicator(
                        color = NetflixRed,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is SearchUiState.Success -> {
                    if (state.results.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.search_no_results),
                                color = NetflixTextSecondary
                            )
                        }
                    } else {
                        SearchResultsGrid(
                            results = state.results,
                            onItemSelected = { item ->
                                onNavigateToDetails(item.title, item.synopsis, item.id, item.type.name.lowercase())
                            }
                        )
                    }
                }
                is SearchUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.onQueryChange(query) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentSearchesList(
    searches: List<String>,
    onSearchClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = stringResource(id = R.string.search_recent),
            style = MaterialTheme.typography.headlineSmall,
            color = NetflixTextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        searches.forEach { search ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSearchClick(search) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_history),
                    contentDescription = null,
                    tint = NetflixTextSecondary
                )
                Text(
                    text = search,
                    color = NetflixTextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun SearchResultsGrid(
    results: List<ContentItem>,
    onItemSelected: (ContentItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = results, key = {it.id}) { item ->
            ContentCard(
                imageUrl = item.posterUrl,
                contentDescription = item.title,
                onClick = { onItemSelected(item) }
            )
        }
    }
}