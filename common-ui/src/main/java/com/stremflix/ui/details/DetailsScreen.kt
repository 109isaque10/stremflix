package com.stremflix.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.stremflix.core.domain.model.ContentType
import com.stremflix.data.model.ContentItem
import com.stremflix.data.model.Episode
import com.stremflix.data.model.Stream
import com.stremflix.ui.components.MatchBadge
import com.stremflix.ui.components.VerticalFadeOverlay
import com.stremflix.ui.R
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixRed
import com.stremflix.ui.theme.NetflixTextPrimary
import com.stremflix.ui.theme.NetflixTextSecondary

@Composable
fun DetailsScreen(
    contentId: String,
    contentType: String,
    onNavigateBack: () -> Unit,
    onNavigateToPlayback: (String, String, String?, String, ContentType) -> Unit,
    isTvMode: Boolean = false,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val streams by viewModel.streams.collectAsState()
    val showStreamDialog by viewModel.showStreamDialog.collectAsState()

    // Collect season/episode state directly
    val seasons by viewModel.seasons.collectAsState()
    val currentSeason by viewModel.currentSeason.collectAsState()
    val episodes by viewModel.episodes.collectAsState()

    if (showStreamDialog) {
        StreamSelectionDialog(
            streams = streams,
            isLoading = streams.isEmpty() && showStreamDialog,
            onDismiss = { viewModel.onStreamSelected(Stream("","", null, null, null)) },
            onStreamSelected = { stream ->
                viewModel.onStreamSelected(stream)
                if (stream != null) {
                    onNavigateToPlayback(stream.url, viewModel.contentTitle, viewModel.contentSynopsis, viewModel.contentId, viewModel.contentType)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixBlack)
    ) {
        when (val state = uiState) {
            is DetailsUiState.Loading -> {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
            is DetailsUiState.Error -> {
                Text(text = state.message, color = NetflixTextSecondary, modifier = Modifier.align(Alignment.Center))
            }
            is DetailsUiState.Success -> {
                DetailsContent(
                    item = state.item,
                    onPlayClick = { viewModel.onPlayClicked(null) },
                    onBackClick = onNavigateBack,
                    seasons = seasons,
                    selectedSeason = currentSeason,
                    episodes = episodes,
                    onSeasonSelected = { viewModel.onSeasonSelected(it) },
                    onEpisodeSelected = { viewModel.onPlayClicked(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun DetailsContent(
    item: ContentItem,
    onPlayClick: () -> Unit,
    onBackClick: () -> Unit,
    seasons: List<Int>,
    selectedSeason: Int,
    episodes: List<Episode>,
    onSeasonSelected: (Int) -> Unit,
    onEpisodeSelected: (Episode) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        // Backdrop with Overlay
        item {
            Box(modifier = Modifier.height(400.dp)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.backdropUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                VerticalFadeOverlay(
                    modifier = Modifier.fillMaxSize(),
                    topAlpha = 0f,
                    bottomAlpha = 1f
                ) { Box(modifier = Modifier.fillMaxSize()) }

                // Back Button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Title and Actions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.displayLarge,
                        color = NetflixTextPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        item.matchScore?.let { MatchBadge(score = it) }
                        Text(text = item.year?.toString() ?: "", color = NetflixTextSecondary)
                        Text(
                            text = item.contentRating ?: "", // Use contentRating for certification
                            color = NetflixTextSecondary,
                            modifier = Modifier
                                .background(Color(0xFF333333), RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Play Button
                        Button(
                            onClick = onPlayClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_play), contentDescription = null, tint = Color.Black)
                                Spacer(Modifier.width(8.dp))
                                Text(text = stringResource(id = R.string.detail_play), color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        // More Info (My List, etc. - simplified)
                        OutlinedButton(
                            onClick = { /* Add to My List */ },
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF333333), contentColor = Color.White),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_list), contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(text = stringResource(id = R.string.detail_add_to_list), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Synopsis & Info
        item {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = item.synopsis ?: "", color = NetflixTextSecondary, style = MaterialTheme.typography.bodyMedium)
                if (item.genres.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(item.genres.joinToString(" • "), color = NetflixTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // TV Show Season/Episode Selector
        if (item.type == ContentType.SERIES && seasons.isNotEmpty()) {
            item {
                SeasonEpisodeSelector(
                    seasons = seasons,
                    selectedSeason = selectedSeason,
                    episodes = episodes,
                    onSeasonSelected = onSeasonSelected,
                    onEpisodeSelected = onEpisodeSelected,
                    modifier = modifier
                )
            }
        }

        // Related/Episodes would go here
        item { Spacer(Modifier.height(100.dp)) }
    }
}
