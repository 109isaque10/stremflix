package com.stremflix.app.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stremflix.app.ui.components.MediaCard
import com.stremflix.core.model.MediaItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateToDetail: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    viewModel.search(it)
                },
                placeholder = { Text("Titles, people, genres") },
                leadingIcon = {
                    Icon(
                        painter = androidx.compose.material.icons.Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()                    .padding(16.dp)
                    .focusRequester(focusRequester),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    containerColor = androidx.compose.ui.graphics.Color(0xFF333333)
                ),
                singleLine = true
            )
            
            // Filter Chips
            if (searchQuery.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.setFilter("all") },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.setFilter("movie") },
                        label = { Text("Movies") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.setFilter("tv") },
                        label = { Text("TV Shows") }
                    )
                }
            }
            
            // Results Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.results) { media ->
                    MediaCard(
                        mediaItem = media,
                        onClick = { onNavigateToDetail(media.id, media.type.name) },
                        modifier = Modifier.width(120.dp)
                    )
                }
            }        }
    }
    
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }
}