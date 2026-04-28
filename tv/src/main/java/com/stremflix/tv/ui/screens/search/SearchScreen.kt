package com.stremflix.tv.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.*
import com.stremflix.tv.ui.components.TvMediaCard

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateToDetail: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    viewModel.search(it)
                },
                placeholder = { Text("Search titles, people, genres") },
                leadingIcon = {
                    Icon(
                        painter = androidx.compose.material.icons.Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .focusRequester(focusRequester),
                colors = androidx.tv.material3.OutlinedTextFieldDefaults.colors()
            )
            
            // Results
            TvLazyVerticalGrid(
                columns = TvGridCells.Adaptive(200.dp),
                contentPadding = PaddingValues(horizontal = 48.dp, vertical = 16.dp),
                horizontalArrangement = androidx.tv.foundation.lazy.list.TvLazyListScope.Arrangement.spacedBy(24.dp),
                verticalArrangement = androidx.tv.foundation.lazy.list.TvLazyListScope.Arrangement.spacedBy(24.dp)
            ) {
                items(uiState.results) { media ->
                    TvMediaCard(
                        mediaItem = media,
                        onClick = { onNavigateToDetail(media.id, media.type.name) }
                    )
                }
            }
        }
    }
}