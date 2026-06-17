package com.stremflix.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.stremflix.core.domain.model.ContentType
import com.stremflix.data.model.ContentItem
import com.stremflix.data.model.Episode
import com.stremflix.data.model.Stream
import com.stremflix.ui.R
import com.stremflix.ui.components.MatchBadge
import com.stremflix.ui.components.VerticalFadeOverlay
import com.stremflix.ui.mylist.MyListViewModel
import com.stremflix.ui.player.TvQualitySelector
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixTextPrimary
import com.stremflix.ui.theme.NetflixTextSecondary

@Composable
fun DetailsScreen(
    contentId: String,
    contentType: String,
    onNavigateBack: () -> Unit,
    onNavigateToPlayback: (String, String, String?, String, ContentType, Boolean) -> Unit,
    isTvMode: Boolean = false,
    scaffoldPadding: PaddingValues,
    viewModel: DetailsViewModel = hiltViewModel(),
    onEpisodes: (String, Int) -> Unit,
    myListViewModel: MyListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val streams by viewModel.streams.collectAsState()
    val showStreamDialog by viewModel.showStreamDialog.collectAsState()

    // Collect season/episode state directly
    val seasons by viewModel.seasons.collectAsState()
    val currentSeason by viewModel.currentSeason.collectAsState()
    val episodes by viewModel.episodes.collectAsState()
    val selectedEpisode by viewModel.selectedEpisode.collectAsState()
    val playFromBeggining by viewModel.playFromBeggining.collectAsState()

    if (showStreamDialog) {
        if(!isTvMode) {
            StreamSelectionDialog(
                streams = streams,
                isLoading = streams.isEmpty() && showStreamDialog,
                onDismiss = { viewModel.onStreamSelected(Stream("", "", null, null, null, null, null)) },
                onStreamSelected = { stream ->
                    viewModel.onStreamSelected(stream)
                    onNavigateToPlayback(
                        stream.url,
                        viewModel.contentTitle,
                        viewModel.contentSynopsis,
                        viewModel.contentId,
                        viewModel.contentType,
                        playFromBeggining
                    )
                }
            )
        } else {
            TvQualitySelector(
                streams = streams,
                isLoading = streams.isEmpty() && showStreamDialog,
                onDismiss = { viewModel.onStreamSelected(Stream("", "", null, null, null, null, null)) },
                onStreamSelected = { stream ->
                    viewModel.onStreamSelected(stream)
                    onNavigateToPlayback(
                        stream.url,
                        viewModel.contentTitle,
                        viewModel.contentSynopsis,
                        viewModel.contentId,
                        viewModel.contentType,
                        playFromBeggining
                    )
                }
            )
        }
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
                if (isTvMode) {
                    DetailsContentTv(
                        item = state.item,
                        onPlayClick = { viewModel.onPlayClicked(selectedEpisode) },
                        onBackClick = onNavigateBack,
                        seasons = seasons,
                        selectedSeason = currentSeason,
                        episodes = episodes,
                        onEpisodeSelected = { viewModel.onPlayClicked(it) },
                        myListViewModel = myListViewModel,
                        onEpisodes = onEpisodes,
                        episodeResume = selectedEpisode,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {

                    DetailsContent(
                        item = state.item,
                        onPlayClick = { viewModel.onPlayClicked(selectedEpisode) },
                        onBackClick = onNavigateBack,
                        seasons = seasons,
                        selectedSeason = currentSeason,
                        episodes = episodes,
                        myListViewModel = myListViewModel,
                        onSeasonSelected = { viewModel.onSeasonSelected(it) },
                        onEpisodeSelected = { viewModel.onPlayClicked(it) },
                        episodeResume = selectedEpisode,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsContent(
    item: ContentItem,
    onPlayClick: () -> Unit,
    onBackClick: () -> Unit,
    myListViewModel: MyListViewModel,
    episodeResume: Episode?,
    seasons: List<Int>,
    selectedSeason: Int,
    episodes: List<Episode>,
    onSeasonSelected: (Int) -> Unit,
    onEpisodeSelected: (Episode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isInList = produceState(false, item) {
        value = myListViewModel.isInMyList(item)
    }.value
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
                        if (item.runtime != null && item.runtime!! > 0) {
                            Text(
                                text = "${item.runtime}m",
                                color = NetflixTextSecondary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Play Button
                        Button(
                            onClick = onPlayClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            val playId = if (item.watchProgress > 0f) R.string.detail_resume else R.string.detail_play
                            var episode = ""
                            if (episodeResume != null) {
                                episode = " S${episodeResume.seasonNumber}E${episodeResume.episodeNumber}"
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_play), contentDescription = null, tint = Color.Black)
                                Spacer(Modifier.width(8.dp))
                                Text(text = stringResource(id = playId)+episode, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        // More Info (My List, etc. - simplified)
                        OutlinedButton(
                            onClick = { if (isInList) myListViewModel.removeFromList(item)
                            else myListViewModel.addToList(item) },
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF333333), contentColor = Color.White),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(imageVector = ImageVector.vectorResource(if(isInList) R.drawable.ic_checkmark else R.drawable.ic_list), contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(text = stringResource(id = if(isInList) R.string.detail_remove_from_list else R.string.detail_add_to_list), fontWeight = FontWeight.Bold, overflow = TextOverflow.Ellipsis, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Synopsis & Info
        item {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = item.synopsis ?: "", color = NetflixTextSecondary, style = MaterialTheme.typography.bodyMedium)
//                Text(text = item.toString(), color = NetflixTextSecondary, style = MaterialTheme.typography.bodyMedium)
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

/**
 * TV-optimized details content.
 *
 * Mirrors the mobile details but uses a left metadata workspace over a high-impact artwork banner
 * with horizontal and vertical scrims, and a vertical action menu where the primary action shows
 * an embedded progress indicator. Keep parameter list compatible with your existing call-site.
 */
@Composable
fun DetailsContentTv(
    item: ContentItem,
    onPlayClick: () -> Unit,
    onBackClick: () -> Unit,
    episodeResume: Episode?,
    seasons: List<Int>,
    selectedSeason: Int,
    episodes: List<Episode>,
    onEpisodeSelected: (Episode) -> Unit,
    onEpisodes: (String, Int) -> Unit,
    myListViewModel: MyListViewModel,
    modifier: Modifier = Modifier,
    onPlayTrailer: (() -> Unit)? = null,
) {
    val isInList = produceState(false, item) {
        value = myListViewModel.isInMyList(item)
    }.value
    val playFocusRequester = remember { FocusRequester() }

    LaunchedEffect(item.id) {
        playFocusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixBlack)
    ) {
        // 1. The Single Cinematic Backdrop covering the entire viewport layer
        item.backdropUrl?.let { backdrop ->
            SubcomposeAsyncImage(
                model = backdrop,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Premium Netflix Dark smoke overlay (Fades left-to-right & bottom-to-top smoothly)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            NetflixBlack,
                            NetflixBlack.copy(alpha = 0.85f),
                            NetflixBlack.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 1400f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, NetflixBlack.copy(alpha = 0.6f), NetflixBlack),
                        startY = 400f
                    )
                )
        )

        // 3. Left-aligned Text Metadata Pane positioned on top of the smoke scrim
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.50f)
                .padding(start = 56.dp, top = 48.dp, bottom = 32.dp, end = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                item.matchScore?.let { MatchBadge(score = it) }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = item.year?.toString() ?: "", color = Color.LightGray)
                item.contentRating?.let { rating ->
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = rating, color = Color.LightGray, modifier = Modifier.border(1.dp, Color.LightGray).padding(horizontal = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = item.synopsis ?: "",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 4
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 4. Premium Vertical Action Menu with Focus Highlights
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // Action A: Play Content / Resume Episode Info
                val playId = if (item.watchProgress > 0f) R.string.detail_resume else R.string.detail_play
                val episodeText = episodeResume?.let { " S${it.seasonNumber}E${it.episodeNumber}" } ?: ""
                TvDetailsMenuButton(
                    text = stringResource(id = playId) + episodeText,
                    iconRes = R.drawable.ic_play,
                    onClick = onPlayClick,
                    modifier = Modifier.focusRequester(playFocusRequester)
                )

                // Action B: Play From Beginning (Visible if item possesses active track layout timeline history)
                if (item.watchProgress > 0f) {
                    TvDetailsMenuButton(
                        text = "Play From Beginning",
                        iconRes = R.drawable.ic_skip_next,
                        onClick = onPlayClick
                    )
                }

                // Action C: Episodes List Navigation Drawer Trigger
                if (item.type == ContentType.SERIES) {
                    TvDetailsMenuButton(
                        text = "Episodes",
                        iconRes = R.drawable.ic_stack,
                        onClick = { onEpisodes(item.id, selectedSeason) }
                    )
                }

                // Action D: Trailer Playback View
                TvDetailsMenuButton(
                    text = "Play Trailer",
                    iconRes = R.drawable.ic_play,
                    onClick = { onPlayTrailer?.invoke() }
                )

                // Action E: Add/Remove Watchlist Tracking
                TvDetailsMenuButton(
                    text = stringResource(id = if (isInList) R.string.detail_remove_from_list else R.string.detail_add_to_list),
                    iconRes = if (isInList) R.drawable.ic_checkmark else R.drawable.ic_list,
                    onClick = {
                        if (isInList) myListViewModel.removeFromList(item)
                        else myListViewModel.addToList(item)
                    }
                )
            }
        }
    }
}

@Composable
private fun TvDetailsMenuButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    androidx.tv.material3.Surface(
        onClick = onClick,
        colors = androidx.tv.material3.ClickableSurfaceDefaults.colors(
            containerColor = if (isFocused) Color.White else Color(0xFF222222),
            focusedContainerColor = Color.White,
            contentColor = if (isFocused) Color.Black else Color.White,
            focusedContentColor = Color.Black
        ),
        shape = androidx.tv.material3.ClickableSurfaceDefaults.shape(RoundedCornerShape(4.dp)),
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.5.dp else 0.dp,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

/** Small helper row with icon, label, and value — used in the info stack. */
@Composable
private fun InfoRow(iconRes: Int, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Icon(imageVector = ImageVector.vectorResource(iconRes), contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelSmall)
            Text(text = value, color = Color.White, style = MaterialTheme.typography.bodySmall)
        }
    }
}