package com.stremflix.tv.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.stremflix.core.model.MediaItem
import com.stremflix.commonui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetail: (String, String) -> Unit,
    onNavigateToPlayer: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var focusedHeroIndex by remember { mutableStateOf(0) }
    var isTrailerPlaying by remember { mutableStateOf(false) }
    val trailerTimeout = viewModel.getTrailerTimeout()
    
    Box(modifier = Modifier.fillMaxSize().background(Black)) {
        // Hero Section with Auto-Play Trailer
        uiState.heroItems?.let { heroes ->
            if (heroes.isNotEmpty()) {
                HeroCarousel(
                    heroes = heroes,
                    currentIndex = focusedHeroIndex,
                    isTrailerPlaying = isTrailerPlaying,
                    onIndexChange = { focusedHeroIndex = it },                    onPlayTrailer = { isTrailerPlaying = true },
                    onStopTrailer = { isTrailerPlaying = false },
                    onPlayClick = { onNavigateToPlayer("") }, // Will be populated
                    onMyListClick = { viewModel.toggleMyList(heroes[focusedHeroIndex].id) },
                    onDetailClick = { 
                        val hero = heroes[focusedHeroIndex]
                        onNavigateToDetail(hero.id, hero.type.name) 
                    },
                    trailerTimeout = trailerTimeout
                )
            }
        }

        // Content Rows
        TvLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 400.dp, start = 48.dp, end = 48.dp, bottom = 48.dp)
        ) {
            uiState.rows.forEach { row ->
                item {
                    TvContentRow(
                        title = row.title,
                        items = row.items,
                        onItemClick = { media ->
                            onNavigateToDetail(media.id, media.type.name)
                        }
                    )
                }
            }
        }

        // Top Navigation
        TopNavigationBar(
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun HeroCarousel(
    heroes: List<MediaItem>,
    currentIndex: Int,
    isTrailerPlaying: Boolean,
    onIndexChange: (Int) -> Unit,
    onPlayTrailer: () -> Unit,
    onStopTrailer: () -> Unit,
    onPlayClick: () -> Unit,
    onMyListClick: () -> Unit,
    onDetailClick: () -> Unit,    trailerTimeout: Long
) {
    val currentHero = heroes[currentIndex]
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(currentIndex) {
        focusRequester.requestFocus()
    }
    
    // Auto-play trailer logic
    LaunchedEffect(currentIndex, isFocused) {
        if (isFocused && !isTrailerPlaying) {
            delay(trailerTimeout * 1000)
            if (isFocused) {
                onPlayTrailer()
            }
        }
    }
    
    LaunchedEffect(isTrailerPlaying) {
        if (isTrailerPlaying) {
            // Simulate trailer duration
            delay(30000) // 30 seconds
            onStopTrailer()
        }
    }

    Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
        // Backdrop or Trailer Video
        if (isTrailerPlaying) {
            // Video player would go here
            // For now, show backdrop with playing indicator
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://image.tmdb.org/t/p/original${currentHero.backdropPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = currentHero.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color(0x40000000))
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)                    .data("https://image.tmdb.org/t/p/original${currentHero.backdropPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = currentHero.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Gradient Overlay (only at bottom for text readability)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Transparent,
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // Hero Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.6f)
                .padding(32.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = currentHero.title,
                style = androidx.tv.material3.MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${currentHero.matchPercent}% Match",
                    color = androidx.compose.ui.graphics.Color(0xFF46D369),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))                Text(text = currentHero.releaseYear ?: "", color = GrayText)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = currentHero.maturityRating,
                    color = GrayText,
                    modifier = Modifier
                        .border(1.dp, GrayText)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                if (currentHero.type == MediaType.SERIES) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "4 Seasons", color = GrayText)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = currentHero.overview,
                style = androidx.tv.material3.MaterialTheme.typography.bodyLarge,
                color = White,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                androidx.tv.material3.Button(
                    onClick = onPlayClick,
                    colors = androidx.tv.material3.ButtonDefaults.buttonColors(
                        containerColor = White,
                        contentColor = Black
                    )
                ) {
                    Text("▶ Play", fontWeight = FontWeight.Bold)
                }
                
                androidx.tv.material3.Button(
                    onClick = onMyListClick,
                    colors = androidx.tv.material3.ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF333333),
                        contentColor = White
                    )
                ) {
                    Text("+ My List", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Hero Indicators        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            heroes.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentIndex) 24.dp else 16.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            if (index == currentIndex) NetflixRed else GrayText.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}

@Composable
fun TvContentRow(
    title: String,
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = androidx.tv.material3.MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        TvLazyRow(
            horizontalArrangement = androidx.tv.foundation.lazy.list.TvLazyListScope.Arrangement.spacedBy(16.dp)
        ) {
            items(items) { media ->
                TvMediaCard(
                    mediaItem = media,
                    onClick = { onItemClick(media) }
                )
            }
        }
    }
}

@Composable
fun TvMediaCard(    mediaItem: MediaItem,
    onClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(158.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused || focusState.hasFocus
            },
        colors = CardDefaults.cardColors(
            containerColor = DarkGray
        ),
        scale = CardDefaults.cardScale(focusedScale = 1.05f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://image.tmdb.org/t/p/w500${mediaItem.posterPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = mediaItem.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NetflixRed.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
fun TopNavigationBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {        Text(
            text = "STREMFLIX",
            style = androidx.tv.material3.MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = NetflixRed
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        listOf("Home", "Search", "My List", "Settings").forEach { tab ->
            androidx.tv.material3.TextButton(onClick = { /* Navigate */ }) {
                Text(tab, color = White)
            }
        }
    }
}