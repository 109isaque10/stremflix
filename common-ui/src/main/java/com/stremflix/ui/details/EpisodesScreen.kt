package com.stremflix.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.stremflix.data.model.ContentItem
import com.stremflix.data.model.Episode
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixTextPrimary
import com.stremflix.ui.theme.NetflixTextSecondary

@Composable
fun EpisodesScreenRoute(
    viewModel: EpisodesViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEpisodeSelected: (Episode) -> Unit
) {
    val item = viewModel.contentItem.collectAsState().value
    val seasons = viewModel.seasons.collectAsState().value
    val currentSeason = viewModel.currentSeason.collectAsState().value
    val episodes = viewModel.episodes.collectAsState().value

    if (item == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    EpisodesScreen(
        item = item,
        seasons = seasons,
        currentSeason = currentSeason,
        onSeasonSelected = { viewModel.onSeasonSelected(it) },
        modules = episodes,
        onBack = onBack,
        onEpisodeSelected = onEpisodeSelected
    )
}

@Composable
fun EpisodesScreen(
    item: ContentItem,
    seasons: List<Int>,
    currentSeason: Int,
    onSeasonSelected: (Int) -> Unit,
    modules: List<Episode>,
    onBack: () -> Unit,
    onEpisodeSelected: (Episode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val seasonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(item.id, currentSeason, seasons) {
        if (seasons.isNotEmpty()) {
            seasonFocusRequester.requestFocus()
        }
    }

    Row(modifier = modifier.fillMaxSize().background(NetflixBlack)) {
        Box(modifier = Modifier.width(420.dp).fillMaxHeight().padding(24.dp)) {
            val art = item.backdropUrl ?: item.posterUrl
            if (!art.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context).data(art).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.25f))
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context).data(item.titleLogoUrl ?: item.title).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(88.dp).padding(bottom = 12.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(text = item.year?.toString().orEmpty(), color = NetflixTextSecondary)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = item.contentRating.orEmpty(),
                        color = NetflixTextSecondary,
                        modifier = Modifier
                            .background(Color(0xFF333333), RoundedCornerShape(2.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(seasons) { season ->
                        SeasonTab(
                            season = season,
                            episodeCount = if (season == currentSeason) modules.size else null,
                            isSelected = season == currentSeason,
                            modifier = if (season == currentSeason) {
                                Modifier.focusRequester(seasonFocusRequester)
                            } else {
                                Modifier
                            },
                            onClick = { onSeasonSelected(season) }
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(top = 24.dp, bottom = 24.dp, end = 24.dp)) {
            Text(
                text = "Episodes",
                color = NetflixTextPrimary,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(
                    items = modules,
                    key = { episode -> "${episode.seriesId}-${episode.seasonNumber}-${episode.episodeNumber}" }
                ) { episode ->
                    EpisodeRow(
                        episode = episode,
                        onClick = { onEpisodeSelected(episode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SeasonTab(
    season: Int,
    episodeCount: Int?,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val active = isFocused || isSelected

    Surface(
        onClick = onClick,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent,
            contentColor = NetflixTextSecondary,
            focusedContentColor = Color.White
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (active) 3.dp else 1.dp,
                color = if (active) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = if (active) 0.72f else 0.35f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Season $season",
                color = if (active) Color.White else NetflixTextSecondary,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.weight(1f))

            if (episodeCount != null) {
                Text(
                    text = "$episodeCount episodes",
                    color = if (active) Color.White else NetflixTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    episode: Episode,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val active = isFocused

    Surface(
        onClick = onClick,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent,
            contentColor = Color.White,
            focusedContentColor = Color.White
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (active) 3.dp else 1.dp,
                color = if (active) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = if (active) 0.72f else 0.45f), RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(220.dp, 124.dp)) {
                val thumb = episode.thumbnailUrl
                if (!thumb.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(thumb).crossfade(true).build(),
                        contentDescription = episode.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize().background(Color(0xFF111111)).padding(0.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier.matchParentSize().background(Color(0xFF111111)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No image", color = NetflixTextSecondary)
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "S${episode.seasonNumber}:E${episode.episodeNumber}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = episode.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = episode.synopsis.orEmpty(),
                    color = NetflixTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = "${episode.runtime ?: 0}m",
                color = NetflixTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
