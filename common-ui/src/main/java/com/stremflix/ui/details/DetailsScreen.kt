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
        StreamSelectionDialog(
            streams = streams,
            isLoading = streams.isEmpty() && showStreamDialog,
            onDismiss = { viewModel.onStreamSelected(Stream("","", null, null, null, null, null)) },
            onStreamSelected = { stream ->
                viewModel.onStreamSelected(stream)
                onNavigateToPlayback(stream.url, viewModel.contentTitle, viewModel.contentSynopsis, viewModel.contentId, viewModel.contentType, playFromBeggining)
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
    val context = LocalContext.current
    val isInList = produceState(false, item) {
        value = myListViewModel.isInMyList(item)
    }.value

//    Box(
//        modifier = modifier
//            .fillMaxSize()
//            .background(NetflixBlack)
//    ) {
//        // Full-bleed artwork
//        val art = item.backdropUrl ?: item.posterUrl
//        if (!art.isNullOrBlank()) {
//            SubcomposeAsyncImage(
//                model = ImageRequest.Builder(context).data(art).crossfade(true).build(),
//                contentDescription = item.title,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.matchParentSize()
//            )
//        } else {
//            Box(modifier = Modifier.matchParentSize().background(Color.DarkGray))
//        }
//
//        // Multi-directional scrims: strong left→transparent and bottom→shadow
//        Box(modifier = Modifier.matchParentSize().drawWithContent {
//            drawContent()
//            val w = size.width
//            val h = size.height
//            val horizontal = Brush.horizontalGradient(
//                colors = listOf(Color.Black.copy(alpha = 0.96f), Color.Transparent),
//                startX = 0f,
//                endX = w * 0.6f
//            )
//            val vertical = Brush.verticalGradient(
//                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f)),
//                startY = h * 0.35f,
//                endY = h
//            )
//            drawRect(brush = horizontal, size = size)
//            drawRect(brush = vertical, size = size)
//        })
//
//        // Foreground content row: left metadata, right area (trailer/artwork)
//        Row(modifier = Modifier.fillMaxSize()) {
//            // Left metadata workspace
//            Box(
//                modifier = Modifier
//                    .width(520.dp)
//                    .fillMaxHeight()
//                    .padding(32.dp)
//            ) {
//                Column(
//                    modifier = Modifier.fillMaxHeight(),
//                    verticalArrangement = Arrangement.SpaceBetween
//                ) {
//                    // Top: title logo + badges + synopsis + info rows
//                    Column {
//                        // Title logo (fallback to text)
//                        SubcomposeAsyncImage(
//                            model = ImageRequest.Builder(context)
//                                .data(item.titleLogoUrl ?: item.title)
//                                .crossfade(true)
//                                .build(),
//                            contentDescription = item.title,
//                            contentScale = ContentScale.Fit,
//                            modifier = Modifier
//                                .height(96.dp)
//                                .padding(bottom = 12.dp),
//                            loading = {
//                                Text(
//                                    text = item.title,
//                                    style = MaterialTheme.typography.displaySmall,
//                                    color = NetflixTextPrimary
//                                )
//                            },
//                            error = {
//                                Text(
//                                    text = item.title,
//                                    style = MaterialTheme.typography.displaySmall,
//                                    color = NetflixTextPrimary
//                                )
//                            }
//                        )
//
//                        Row(
//                            modifier = Modifier.padding(bottom = 12.dp),
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            item.matchScore?.let { MatchBadge(score = it) }
//                            Text(text = item.year?.toString() ?: "", color = NetflixTextSecondary)
//                            if (item.type == ContentType.SERIES) {
//                                Text(text = item.numberOfSeasons?.let { "$it ${R.string.detail_seasons}" } ?: "", color = NetflixTextSecondary)
//                            }
//                            Text(
//                                text = item.contentRating ?: "",
//                                color = NetflixTextSecondary,
//                                modifier = Modifier
//                                    .background(Color(0xFF333333), RoundedCornerShape(2.dp))
//                                    .padding(horizontal = 4.dp, vertical = 2.dp)
//                            )
//                        }
//
//                        Spacer(Modifier.height(6.dp))
//
//                        // Synopsis
//                        Text(
//                            text = item.synopsis ?: "",
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = NetflixTextSecondary,
//                            maxLines = 6
//                        )
//
//                        Spacer(Modifier.height(16.dp))
//
//                        // Detailed info stack: cast, contributors, genres, accolades
//                        LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
//                            item {
//                                InfoRow(iconRes = R.drawable.ic_star, label = "Cast", value = (item.cast.joinToString(", ")
//                                    ?: "—"))
//                            }
//                            item {
//                                InfoRow(iconRes = R.drawable.ic_masks, label = "Genres", value = (item.genres.joinToString(" • ") ?: "—"))
//                            }
//                        }
//                    }
//
//                    // Bottom: vertical action menu (primary + secondary)
//                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                        // Primary action: prominent rectangular button with progress bar
//                        Card(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(80.dp)
//                                .focusable(true)
//                                .clickable { onPlayClick() }
//                                .border(2.dp, Color.White.copy(alpha = 0.12f)),
//                            colors = CardDefaults.cardColors(containerColor = Color.White),
//                            shape = RoundedCornerShape(6.dp)
//                        ) {
//                            Row(
//                                modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                val playId = if (item.watchProgress > 0f) R.string.detail_resume else R.string.detail_play
//                                var episode = ""
//                                if (episodeResume != null) {
//                                    episode = " S${episodeResume.seasonNumber} E${episodeResume.episodeNumber}"
//                                }
//
//                                Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_play), contentDescription = null, tint = Color.Black)
//                                Spacer(Modifier.width(8.dp))
//
//                                Text(
//                                    text = stringResource(id = playId)+episode,
//                                    color = Color.Black,
//                                    style = StremFlixTypography.bodyMedium,
//                                    modifier = Modifier.weight(1f)
//                                )
//
//                                // small embedded progress track
//                                LinearProgressIndicator(
//                                    progress = item.watchProgress,
//                                    modifier = Modifier.width(200.dp).height(8.dp).padding(start = 12.dp),
//                                    color = NetflixRed,
//                                    trackColor = Color.Black.copy(alpha = 0.08f)
//                                )
//                            }
//                        }
//
//                        if(item.watchProgress > 0f) {
//                            Row(modifier = Modifier
//                                .fillMaxWidth()
//                                .height(56.dp)
//                                .clickable { onPlayClick() }
//                                .focusable(true)
//                                .padding(horizontal = 8.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_skip_next), contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp).scale(scaleX = -1f, scaleY = 1f))
//                                Spacer(Modifier.width(12.dp))
//                                Text(text = "Play From Beggining", color = Color.White, style = StremFlixTypography.bodyMedium)
//                            }
//                        }
//
//                        if(item.type == ContentType.SERIES)
//                            Row(modifier = Modifier
//                                .fillMaxWidth()
//                                .height(56.dp)
//                                .clickable { onEpisodes.invoke(item.id, selectedSeason) }
//                                .focusable(true)
//                                .padding(horizontal = 8.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_stack), contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
//                                Spacer(Modifier.width(12.dp))
//                                Text(text = "Episodes", color = Color.White, style = StremFlixTypography.bodyMedium)
//                            }
//
//                        Row(modifier = Modifier
//                            .fillMaxWidth()
//                            .height(56.dp)
//                            .clickable { onPlayTrailer?.invoke() }
//                            .focusable(true)
//                            .padding(horizontal = 8.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_play), contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
//                            Spacer(Modifier.width(12.dp))
//                            Text(text = "Play Trailer", color = Color.White, style = StremFlixTypography.bodyMedium)
//                        }
//
//                        Row(modifier = Modifier
//                            .fillMaxWidth()
//                            .height(56.dp)
//                            .clickable { if (isInList) myListViewModel.removeFromList(item)
//                            else myListViewModel.addToList(item) }
//                            .focusable(true)
//                            .padding(horizontal = 8.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_play), contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
//                            Spacer(Modifier.width(12.dp))
//                            Text(text = stringResource(id = if(isInList) R.string.detail_remove_from_list else R.string.detail_add_to_list), color = Color.White, style = StremFlixTypography.bodyMedium)
//                        }
//                    }
//                }
//            }
//
//            // Right area: trailer preview or artwork (fills remaining space)
//            Box(modifier = Modifier.fillMaxHeight().weight(1f)) {
//                // We intentionally keep this as artwork/trailer area — hero/trailer can be embedded similarly to HeroCardTv.
//                // For brevity we render artwork; if you want the YouTube trailer here reuse HeroCardTv's AndroidView + poster overlay logic.
//                val artRight = item.backdropUrl ?: item.posterUrl
//                if (!artRight.isNullOrBlank()) {
//                    SubcomposeAsyncImage(
//                        model = ImageRequest.Builder(LocalContext.current).data(artRight).crossfade(true).build(),
//                        contentDescription = item.title,
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier.fillMaxSize()
//                    )
//                } else {
//                    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
//                }
//            }
//        }
//    }
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
                .fillMaxWidth(0.55f) // Keeps content bounded neatly to the legible left partition zone
                .padding(start = 56.dp, top = 48.dp, bottom = 32.dp, end = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.title,
                style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                item.matchScore?.let { MatchBadge(score = it) }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = item.year.toString(), color = Color.LightGray)
                item.contentRating?.let { rating ->
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = rating, color = Color.LightGray, modifier = Modifier.border(1.dp, Color.LightGray).padding(horizontal = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = item.synopsis ?: "",
                color = Color.LightGray,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                maxLines = 4
            )
            Spacer(modifier = Modifier.height(32.dp))

            // 4. Play Action Button featuring standard Netflix Focus-border Highlight Box
            var isPlayFocused by remember { mutableStateOf(false) }
            androidx.tv.material3.Surface(
                onClick = onPlayClick,
                colors = androidx.tv.material3.ClickableSurfaceDefaults.colors(
                    containerColor = Color.White,
                    focusedContainerColor = Color.White,
                    contentColor = Color.Black,
                    focusedContentColor = Color.Black
                ),
                shape = androidx.tv.material3.ClickableSurfaceDefaults.shape(RoundedCornerShape(4.dp)),
                modifier = Modifier
                    .width(160.dp)
                    .height(48.dp)
                    .onFocusChanged { isPlayFocused = it.isFocused }
                    .border(
                        width = if (isPlayFocused) 3.dp else 0.dp,
                        color = if (isPlayFocused) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Play", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                }
            }
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