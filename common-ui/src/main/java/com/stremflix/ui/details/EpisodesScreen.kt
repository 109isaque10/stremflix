package com.stremflix.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.stremflix.data.model.ContentItem
import com.stremflix.data.model.Episode
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixTextPrimary
import com.stremflix.ui.theme.NetflixTextSecondary

@Composable
fun EpisodesScreenRoute(
    // this wrapper is for NavGraph usage; you can call EpisodesScreenRoute() in nav graph
    viewModel: EpisodesViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEpisodeSelected: (Episode) -> Unit
) {
    val item = viewModel.contentItem.collectAsState().value
    val seasons = viewModel.seasons.collectAsState().value
    val currentSeason = viewModel.currentSeason.collectAsState().value
    val episodes = viewModel.episodes.collectAsState().value

    // If contentItem == null, you can show a loading placeholder
    if (item == null) {
        // show loading or placeholder
        CircularProgressIndicator()
        return
    }

    // Reuse the UI you already built earlier (left column + right list). If you implemented EpisodeRow/EpisodesScreen UI in another file,
    // call it here. For clarity, assume you have a composable `EpisodesScreenUi` that takes item, categories and modules.
//    val categories = listOf("All Episodes" to episodes.size) // build real categories if you have them
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

/**
 * Episodes exploration split-view:
 * left column: logo + badges + vertical category selector
 * right column: vertical list of content modules with thumbnail + text block
 *
 * Provide `categories` as list of Pair(label, count) for left selector.
 */
@Composable
fun EpisodesScreen(
    item: ContentItem,
    seasons: List<Int>,
    currentSeason: Int,
    onSeasonSelected: (Int) -> Unit,
    modules: List<Episode>, // episodes or items to display on the right
    onBack: () -> Unit,
    onEpisodeSelected: (Episode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
//    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    Row(modifier = modifier.fillMaxSize().background(NetflixBlack)) {
        // Left column: dimmed artwork backdrop + metadata & category selector
        Box(modifier = Modifier.width(420.dp).fillMaxHeight().padding(20.dp)) {
            // Dimmed artwork as background
            val art = item.backdropUrl ?: item.posterUrl
            if (!art.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context).data(art).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().alpha(0.28f)
                )
            }
            Column(modifier = Modifier.fillMaxSize()) {
                // Title logo
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context).data(item.titleLogoUrl ?: item.title).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(96.dp).padding(bottom = 12.dp)
                )

                // performance metrics & maturity badge row (placeholder)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(text = item.year?.toString() ?: "", color = NetflixTextSecondary)
                    Spacer(Modifier.width(10.dp))
                    Text(text = item.contentRating ?: "", color = NetflixTextSecondary, modifier = Modifier.background(Color(0xFF333333), RoundedCornerShape(2.dp)).padding(4.dp))
                }

                Spacer(Modifier.height(12.dp))

                // Category selector (vertical list)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(seasons) { season ->
//                        val isSelected = season == currentSeason
                        var isFocused = season == currentSeason // For simplicity, using currentSeason as focused state. You can manage focus separately if needed.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .then(if (isFocused) Modifier.border(3.dp, Color.White, RoundedCornerShape(0.dp)) else Modifier)
                                .clickable { onSeasonSelected(season) }
                                .focusable(true)
                                .onFocusChanged { focusState -> isFocused = focusState.isFocused }
                                .padding(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Season $season", color = if (isFocused) Color.White else NetflixTextSecondary, modifier = Modifier.weight(1f))
//                                Text(text = modules.num.toString(), color = if (isSelected) Color.White else NetflixTextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Right column: scrollable modules list
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text(text = "Episodes", color = NetflixTextPrimary, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(modules) { ep ->
                    EpisodeRow(episode = ep, index = modules.indexOf(ep), onClick = { onEpisodeSelected(ep) })
                }
            }
        }
    }
}

@Composable
private fun EpisodeRow(episode: Episode, index: Int, onClick: () -> Unit) {
    var isFocused = false
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)
        .clickable { onClick() }
        .background(Color(0x11000000), RoundedCornerShape(0.dp))
        .onFocusChanged { focusState -> isFocused = focusState.isFocused }
        .then(if (isFocused) Modifier.border(3.dp, Color.White, RoundedCornerShape(0.dp)) else Modifier)
        .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with index watermark
        Box(modifier = Modifier.width(180.dp).fillMaxHeight()) {
            val thumb = episode.thumbnailUrl
            if (!thumb.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = thumb,
                    contentDescription = episode.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().clip(RoundedCornerShape(6.dp))
                )
            } else {
                Box(modifier = Modifier.matchParentSize().background(Color.DarkGray).clip(RoundedCornerShape(6.dp)))
            }

            // Watermark index bottom-left
            Box(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp)).padding(6.dp)) {
                Text(text = "#${index + 1}", color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.width(12.dp))

        // Text block
        Column(modifier = Modifier.weight(1f)) {
            Text(text = episode.title ?: "Episode", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(text = episode.synopsis ?: "", color = NetflixTextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 3)
        }

        // Duration
        Text(text = "${episode.runtime ?: 0}m", color = NetflixTextSecondary)
    }
}