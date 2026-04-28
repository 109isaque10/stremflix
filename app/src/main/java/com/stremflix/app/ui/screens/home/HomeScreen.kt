package com.stremflix.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.stremflix.app.ui.components.BottomNavigationBar
import com.stremflix.app.ui.components.MediaCard
import com.stremflix.app.ui.components.ProgressIndicator
import com.stremflix.core.model.MediaItem
import com.stremflix.core.model.MediaType
import com.stremflix.commonui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetail: (String, String) -> Unit,
    onNavigateToPlayer: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Search", "Coming Soon", "Downloads", "More")

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                tabs = tabs,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {            // Hero Section
            item {
                uiState.heroItem?.let { hero ->
                    HeroCard(
                        mediaItem = hero,
                        onPlayClick = { /* Handle play */ },
                        onMyListClick = { viewModel.toggleMyList(hero.id) },
                        onDetailClick = { onNavigateToDetail(hero.id, hero.type.name) }
                    )
                }
            }

            // Content Rows
            items(uiState.rows) { row ->
                ContentRow(
                    title = row.title,
                    items = row.items,
                    onItemClick = { media ->
                        onNavigateToDetail(media.id, media.type.name)
                    }
                )
            }
        }
    }
}

@Composable
fun HeroCard(
    mediaItem: MediaItem,
    onPlayClick: () -> Unit,
    onMyListClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Backdrop Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://image.tmdb.org/t/p/original${mediaItem.backdropPath}")
                .crossfade(true)
                .build(),
            contentDescription = mediaItem.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        )

        // Gradient Overlay
        Box(
            modifier = Modifier                .fillMaxWidth()
                .height(500.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f),
                            Color.Black
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp)
                .align(Alignment.BottomStart),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Title Logo/Text
            Text(
                text = mediaItem.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Metadata Row
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${mediaItem.matchPercent}% Match",
                    color = androidx.compose.ui.graphics.Color(0xFF46D369),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mediaItem.releaseYear ?: "",
                    color = GrayText
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mediaItem.maturityRating,
                    color = GrayText,                    modifier = Modifier
                        .border(1.dp, GrayText)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mediaItem.qualityTag,
                    color = GrayText,
                    modifier = Modifier
                        .border(1.dp, GrayText)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Synopsis
            Text(
                text = mediaItem.overview,
                style = MaterialTheme.typography.bodyMedium,
                color = White,
                maxLines = 3,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Action Buttons
            Row {
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White,
                        contentColor = Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("▶ Play", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onMyListClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+ My List", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
@Composable
fun ContentRow(
    title: String,
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { media ->
                MediaCard(
                    mediaItem = media,
                    onClick = { onItemClick(media) },
                    modifier = Modifier.width(120.dp)
                )
            }
        }
    }
}