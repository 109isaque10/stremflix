package com.stremflix.tv.ui.screens.detail

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.stremflix.core.model.Episode
import com.stremflix.core.model.MediaType
import com.stremflix.commonui.theme.*

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
    val playFocusRequester = remember { FocusRequester() }
    
    LaunchedEffect(mediaId, mediaType) {
        viewModel.loadDetail(mediaId, MediaType.valueOf(mediaType))
    }

    Box(modifier = Modifier.fillMaxSize().background(Black)) {
        // Backdrop
        uiState.mediaItem?.let { media ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://image.tmdb.org/t/p/original${media.backdropPath}")                    .crossfade(true)
                    .build(),
                contentDescription = media.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Black.copy(alpha = 0.7f))
            )
            
            // Content Panel
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(64.dp),
                horizontalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                // Left Panel - Metadata and Actions
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${media.matchPercent}% Match",
                            color = androidx.compose.ui.graphics.Color(0xFF46D369),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = media.releaseYear ?: "", color = GrayText)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = media.maturityRating,
                            color = GrayText,
                            modifier = Modifier
                                .border(1.dp, GrayText)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = media.qualityTag, color = GrayText)                    }
                    
                    Text(
                        text = media.overview,
                        style = MaterialTheme.typography.bodyLarge,
                        color = White,
                        maxLines = 5
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                uiState.streamUrl?.let { url ->
                                    onNavigateToPlayer(url)
                                }
                            },
                            modifier = Modifier.focusRequester(playFocusRequester),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = White,
                                contentColor = Black
                            )
                        ) {
                            Text("▶ Play", fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = { viewModel.toggleMyList(media.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.ui.graphics.Color(0xFF333333),
                                contentColor = White
                            )
                        ) {
                            Text("+ My List")
                        }
                        
                        Button(
                            onClick = { /* Rate */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.ui.graphics.Color(0xFF333333),
                                contentColor = White
                            )
                        ) {
                            Text("Rate")
                        }
                    }
                    
                    // Cast
                    if (uiState.cast.isNotEmpty()) {
                        Text(
                            text = "Cast",                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = White,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        TvLazyRow(
                            horizontalArrangement = androidx.tv.foundation.lazy.list.TvLazyListScope.Arrangement.spacedBy(24.dp)
                        ) {
                            items(uiState.cast) { actor ->
                                TvCastCard(actor = actor)
                            }
                        }
                    }
                }
                
                // Right Panel - Episodes (for Series)
                if (media.type == MediaType.SERIES && uiState.episodes.isNotEmpty()) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Episodes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Season Selector
                        var seasonExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = seasonExpanded,
                            onExpandedChange = { seasonExpanded = it },
                            modifier = Modifier.padding(bottom = 16.dp).width(200.dp)
                        ) {
                            OutlinedTextField(
                                value = "Season $selectedSeason",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = seasonExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = seasonExpanded,
                                onDismissRequest = { seasonExpanded = false }
                            ) {
                                (1..4).forEach { season ->
                                    DropdownMenuItem(
                                        text = { Text("Season $season", color = White) },                                        onClick = {
                                            selectedSeason = season
                                            seasonExpanded = false
                                            viewModel.loadEpisodes(media.id, season)
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Episode List
                        TvLazyColumn(
                            verticalArrangement = androidx.tv.foundation.lazy.list.TvLazyListScope.Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.episodes) { episode ->
                                TvEpisodeCard(
                                    episode = episode,
                                    onClick = {
                                        viewModel.getEpisodeStream(media.id, episode.seasonNumber, episode.episodeNumber)
                                            ?.let { url -> onNavigateToPlayer(url) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Back Button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.align(Alignment.TopStart).padding(32.dp)
        ) {
            Icon(
                painter = androidx.compose.material.icons.Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = White
            )
        }
    }
}

@Composable
fun TvCastCard(actor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CardDefaults.shape)                .background(androidx.compose.ui.graphics.Color(0xFF333333))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = actor,
            style = MaterialTheme.typography.bodyMedium,
            color = White
        )
    }
}

@Composable
fun TvEpisodeCard(
    episode: Episode,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color(0xFF1A1A1A)
        )
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(90.dp)
                    .clip(CardDefaults.shape)
                    .background(androidx.compose.ui.graphics.Color(0xFF333333))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${episode.episodeNumber}. ${episode.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    text = "${episode.durationMinutes} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText
                )
                Text(
                    text = episode.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText,                    maxLines = 2
                )
            }
        }
    }
}