package com.stremflix.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.stremflix.app.ui.components.MediaCard
import com.stremflix.core.model.Episode
import com.stremflix.core.model.MediaItem
import com.stremflix.core.model.MediaType
import com.stremflix.commonui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    mediaId: String,
    mediaType: String,
    viewModel: DetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedSeason by remember { mutableStateOf(1) }
    var expandedSynopsis by remember { mutableStateOf(false) }
    
    LaunchedEffect(mediaId, mediaType) {
        viewModel.loadDetail(mediaId, MediaType.valueOf(mediaType))
    }

    ModalBottomSheet(
        onDismissRequest = onNavigateBack,
        containerColor = DarkGray,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Backdrop Image
            item {
                uiState.mediaItem?.let { media ->
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("https://image.tmdb.org/t/p/original${media.backdropPath}")
                                .crossfade(true)
                                .build(),
                            contentDescription = media.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            androidx.compose.ui.graphics.Color.Transparent,
                                            DarkGray
                                        )
                                    )
                                )
                        )
                        
                        // Title Logo
                        Text(
                            text = media.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = White,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        )
                    }
                }
            }
            
            // Metadata and Actions
            item {
                uiState.mediaItem?.let { media ->
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Metadata Row
                        Row(
                            modifier = Modifier.padding(bottom = 16.dp),                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${media.matchPercent}% Match",
                                color = androidx.compose.ui.graphics.Color(0xFF46D369),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = media.releaseYear ?: "", color = GrayText)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = media.maturityRating,
                                color = GrayText,
                                modifier = Modifier
                                    .border(1.dp, GrayText)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = media.qualityTag, color = GrayText)
                            if (media.type == MediaType.SERIES) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "4 Seasons", color = GrayText)
                            }
                        }
                        
                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { 
                                    uiState.streamUrl?.let { url ->
                                        onNavigateToPlayer(url)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = White,
                                    contentColor = Black
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("▶ Play", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { viewModel.toggleMyList(media.id) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = White
                                ),
                                modifier = Modifier.weight(1f)                            ) {
                                Text("+ My List")
                            }
                            OutlinedButton(
                                onClick = { /* Rate */ },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = White
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Rate")
                            }
                        }
                        
                        // Synopsis
                        Text(
                            text = if (expandedSynopsis || media.overview.length < 150) {
                                media.overview
                            } else {
                                media.overview.take(150) + "..."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (media.overview.length >= 150) {
                            TextButton(onClick = { expandedSynopsis = !expandedSynopsis }) {
                                Text(
                                    text = if (expandedSynopsis) "Show less" else "More info",
                                    color = NetflixRed
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Cast
                        if (uiState.cast.isNotEmpty()) {
                            Text(
                                text = "Cast",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(uiState.cast) { actor ->                                    CastCard(actor = actor)
                                }
                            }
                        }
                        
                        // Genres
                        if (media.genres.isNotEmpty()) {
                            Text(
                                text = "Genres",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                media.genres.forEach { genre ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(genre, color = White) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = androidx.compose.ui.graphics.Color(0xFF333333)
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Episodes (for Series)
                        if (media.type == MediaType.SERIES && uiState.episodes.isNotEmpty()) {
                            Text(
                                text = "Episodes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            // Season Selector
                            var seasonExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = seasonExpanded,
                                onExpandedChange = { seasonExpanded = it },
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                OutlinedTextField(
                                    value = "Season $selectedSeason",                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = seasonExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = seasonExpanded,
                                    onDismissRequest = { seasonExpanded = false }
                                ) {
                                    (1..4).forEach { season ->
                                        DropdownMenuItem(
                                            text = { Text("Season $season", color = White) },
                                            onClick = {
                                                selectedSeason = season
                                                seasonExpanded = false
                                                viewModel.loadEpisodes(media.id, season)
                                            }
                                        )
                                    }
                                }
                            }
                            
                            // Episode List
                            uiState.episodes.forEach { episode ->
                                EpisodeCard(
                                    episode = episode,
                                    onClick = {
                                        viewModel.getEpisodeStream(media.id, episode.seasonNumber, episode.episodeNumber)
                                            ?.let { url -> onNavigateToPlayer(url) }
                                    }
                                )
                            }
                        }
                        
                        // More Like This
                        if (uiState.similarItems.isNotEmpty()) {
                            Text(
                                text = "More Like This",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.similarItems) { similar ->                                    MediaCard(
                                        mediaItem = similar,
                                        onClick = { /* Navigate to detail */ },
                                        modifier = Modifier.width(120.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CastCard(actor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(50))
                .background(androidx.compose.ui.graphics.Color(0xFF333333))
        ) {
            // Actor image would go here
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = actor,
            style = MaterialTheme.typography.bodySmall,
            color = White,
            maxLines = 1
        )
    }
}

@Composable
fun EpisodeCard(
    episode: Episode,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color(0xFF1A1A1A)
        )
    ) {        Row(modifier = Modifier.padding(12.dp)) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(68.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(androidx.compose.ui.graphics.Color(0xFF333333))
            ) {
                if (episode.thumbnailPath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://image.tmdb.org/t/p/w500${episode.thumbnailPath}")
                            .build(),
                        contentDescription = episode.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Episode Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${episode.episodeNumber}. ${episode.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    text = "${episode.durationMinutes}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText
                )
                Text(
                    text = episode.overview,
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}