package com.stremflix.ui.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stremflix.data.model.ContentItem
import com.stremflix.ui.R
import com.stremflix.ui.components.ContentCard
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixSurfaceLight
import com.stremflix.ui.theme.NetflixTextPrimary
import com.stremflix.ui.theme.NetflixTextSecondary

@Composable
fun CategoryBrowseScreen(
    viewModel: CategoryBrowseViewModel = hiltViewModel(),
    onNavigateToDetails: (String, String?, String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

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
                    Text(
                        text = selectedCategory ?: stringResource(id = R.string.nav_categories),
                        style = MaterialTheme.typography.headlineMedium,
                        color = NetflixTextPrimary
                    )
                }
            }
        }
    ) { padding ->
        if (selectedCategory == null) {
            // Show Category List
            CategoryList(
                categories = viewModel.categories,
                onSelectCategory = { viewModel.selectCategory(it) },
                modifier = Modifier.padding(padding)
            )
        } else {
            // Show Content Grid for Category
            when (val state = uiState) {
                is CategoryBrowseUiState.Loading -> {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.padding(padding).wrapContentSize(Alignment.Center))
                }
                is CategoryBrowseUiState.Success -> {
                    ContentGrid(
                        items = state.items,
                        onItemSelected = { item ->
                            onNavigateToDetails(item.title, item.synopsis, item.id, item.type.name.lowercase())
                        },
                        modifier = Modifier.padding(padding)
                    )
                }
                is CategoryBrowseUiState.Error -> {
                    Text(text = state.message, color = NetflixTextSecondary, modifier = Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
private fun CategoryList(
    categories: List<String>,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(categories) { category ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
                    .clickable { onSelectCategory(category) },
                color = NetflixSurfaceLight,
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleLarge,
                        color = NetflixTextPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentGrid(
    items: List<ContentItem>,
    onItemSelected: (ContentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(items) { item ->
            ContentCard(
                imageUrl = item.posterUrl,
                contentDescription = item.title,
                onClick = { onItemSelected(item) }
            )
        }
    }
}