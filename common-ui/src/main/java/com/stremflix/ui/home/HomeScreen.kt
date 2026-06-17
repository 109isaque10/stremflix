package com.stremflix.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stremflix.ui.R
import com.stremflix.ui.components.GenreCard
import com.stremflix.ui.components.LoadingSkeleton
import com.stremflix.ui.details.StreamSelectionDialog
import com.stremflix.ui.movies.MoviesViewModel
import com.stremflix.ui.player.TvQualitySelector
import com.stremflix.ui.series.SeriesViewModel
import com.stremflix.ui.theme.*
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HomeScreen(
    onNavigateToDetails: (String, String?, String, String) -> Unit,
    onNavigateToPlayback: (String, String, String?, String, String) -> Unit,
    onNavigateToCategory: (String?) -> Unit,
    isTvMode: Boolean = false,
    filterType: String = "home",
    homeViewModel: HomeViewModel = hiltViewModel(),
    moviesViewModel: MoviesViewModel = hiltViewModel(),
    seriesViewModel: SeriesViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit
) {
    val homeState by homeViewModel.uiState.collectAsState()
    val moviesState by moviesViewModel.uiState.collectAsState()
    val seriesState by seriesViewModel.uiState.collectAsState()

//    LaunchedEffect(Unit) {
//        homeViewModel.loadHomeContent()
//    }

    val unifiedState: HomeUiState = remember(filterType, homeState, moviesState, seriesState) {
        when (filterType) {
            "movie" -> when (moviesState) {
                is MoviesUiState.Loading -> HomeUiState.Loading
                is MoviesUiState.Success -> HomeUiState.Success((moviesState as MoviesUiState.Success).rows)
                is MoviesUiState.Error -> HomeUiState.Error((moviesState as MoviesUiState.Error).message)
            }

            "tv" -> when (seriesState) {
                is SeriesUiState.Loading -> HomeUiState.Loading
                is SeriesUiState.Success -> HomeUiState.Success((seriesState as SeriesUiState.Success).rows)
                is SeriesUiState.Error -> HomeUiState.Error((seriesState as SeriesUiState.Error).message)
            }

            else -> homeState // Default to standard Home
        } as HomeUiState
    }

    val showStreamDialog by homeViewModel.showStreamDialog.collectAsState()
    val streams by homeViewModel.streams.collectAsState()

    val contentFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    LaunchedEffect(unifiedState) {
        if (unifiedState is HomeUiState.Success) {
            kotlinx.coroutines.delay(150.milliseconds) // Allow list compilation matrix to settle
            try { contentFocusRequester.requestFocus() } catch (e: Exception) {}
        }
    }

    // Observe the correct state based on the tab!
    val rows = when (filterType) {
        "movie" -> moviesViewModel.uiState.collectAsState().value.let { if (it is MoviesUiState.Success) it.rows else emptyList() }
        "tv" -> seriesViewModel.uiState.collectAsState().value.let { if (it is SeriesUiState.Success) it.rows else emptyList() }
        else -> homeViewModel.uiState.collectAsState().value.let { if (it is HomeUiState.Success) it.rows else emptyList() }
    }

    if (showStreamDialog) {
        if(!isTvMode){
            StreamSelectionDialog(
                streams = streams,
                onDismiss = { homeViewModel.dismissStreamDialog() },
                onStreamSelected = { stream ->
                    homeViewModel.onStreamSelected(stream)
                    // Get the current hero item (you'll need to track this in ViewModel)
                    // For now, navigate with placeholder values
                    if (stream != null) {
                        // You need to store the selected item in ViewModel
                        val item = homeViewModel.currentSelectedItem
                        if (item != null) {
                            onNavigateToPlayback(stream.url, item.title, item.synopsis, item.id, item.type.name.lowercase())
                        }
                    }
                }
            )
        } else {
            TvQualitySelector(
                streams = streams,
                onDismiss = { homeViewModel.dismissStreamDialog() },
                onStreamSelected = { stream ->
                    homeViewModel.onStreamSelected(stream)
                    // Get the current hero item (you'll need to track this in ViewModel)
                    // For now, navigate with placeholder values
                    if (stream != null) {
                        // You need to store the selected item in ViewModel
                        val item = homeViewModel.currentSelectedItem
                        if (item != null) {
                            onNavigateToPlayback(stream.url, item.title, item.synopsis, item.id, item.type.name.lowercase())
                        }
                    }
                }
            )
        }
    }

    Scaffold(topBar = {
        if (!isTvMode){
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.tv_banner), // Or stremflix_logo if you prefer
                            contentDescription = "StremFlix",
                            modifier = Modifier.height(48.dp)
                        )

                        val titleStr = when(filterType) {
                            "tv" -> stringResource(R.string.nav_tv_shows)
                            "movie" -> stringResource(R.string.nav_movies)
                            "list" -> stringResource(R.string.nav_my_list)
                            else -> ""
                        }

                        if (titleStr.isNotEmpty()) {
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = titleStr,
                                color = NetflixTextPrimary,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                },
                actions = {
                    // Settings button in top bar
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_settings),
                            contentDescription = "Settings",
                            tint = NetflixTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NetflixBlack,
                    titleContentColor = NetflixTextPrimary
                )
            )
        }
    }){
        padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)  // ADD THIS to prevent content cutting
                .background(NetflixBlack)
        ) {
            when (unifiedState) {
                is HomeUiState.Loading -> {
                    // HomeScreenSkeleton()
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
												CircularProgressIndicator(color = NetflixRed)
										}
                }
                is HomeUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_error),
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Error Loading Content",
                            style = MaterialTheme.typography.headlineMedium,
                            color = NetflixTextPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = unifiedState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NetflixTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is HomeUiState.Success -> {
                    if (rows.isEmpty()) {
                        // Empty state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No content available",
                                style = StremFlixTypography.bodyLarge,
                                color = NetflixTextSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = if (isTvMode) 16.dp else 0.dp,
                                bottom = 100.dp
                            )
                        ) {
                            // Hero section
                            val heroRow = rows.firstOrNull { it.items.isNotEmpty() }
                            if (heroRow != null && heroRow.items.isNotEmpty() && filterType == "home") {
                                item {
                                    HeroCard(
                                        item = heroRow.items.first(),
                                        onPlayClick = { homeViewModel.onPlayClicked(null, heroRow.items.first()) },
                                        onMoreInfoClick = {
                                            onNavigateToDetails(
                                                heroRow.items.first().title,
                                                heroRow.items.first().synopsis,
                                                heroRow.items.first().id,
                                                heroRow.items.first().type.name.lowercase()
                                            )
                                        },
                                        isTv = isTvMode,
                                        modifier = Modifier
                                            .fillParentMaxWidth()
                                            .padding(horizontal = if (isTvMode) 24.dp else 0.dp)
                                            .focusRequester(contentFocusRequester)
                                    )
                                    Spacer(Modifier.height(24.dp))
                                }
                            }

                            // Content rows
                            items(
                                items = unifiedState.rows,
                                key = { it.title }
                            ) { rowData ->
                                val isFirstRow = unifiedState.rows.firstOrNull()?.title == rowData.title
                                ContentRow(
                                    title = rowData.title,
                                    items = rowData.items,
                                    isLarge = rowData.isLarge,
                                    isTvMode = isTvMode,
                                    onItemSelected = { item ->
                                        onNavigateToDetails(item.title, item.synopsis, item.id, item.type.name.lowercase())
                                    },
                                    modifier = if (isFirstRow && (heroRow == null || filterType != "home")) {
                                        Modifier.focusRequester(contentFocusRequester)
                                    } else Modifier
                                )
                            }

                            if(!isTvMode && filterType == "home"){
                                item {
                                    val genres = com.stremflix.data.util.TmdbGenres.MOVIE.values.sorted()

                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                                        Text(
                                            text = "Browse by Genre",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                                        )

                                        LazyRow(
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(genres.size) { index ->
                                                val genre = genres[index]
                                                GenreCard(
                                                    genreName = genre,
                                                    onClick = {
                                                        // You would pass the genre ID or name to your navigation here
                                                        onNavigateToCategory(genre)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item { Spacer(Modifier.height(100.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(NetflixBlack),
        userScrollEnabled = false
    ) {
        // Hero Skeleton (The big poster at the top)
        item {
            LoadingSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
            )
            Spacer(Modifier.height(24.dp))
        }

        // Row Skeletons (The horizontal lists)
        items(4) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                // Category Title Skeleton
                LoadingSkeleton(
                    modifier = Modifier
                        .padding(start = 16.dp, bottom = 8.dp)
                        .height(20.dp)
                        .width(150.dp)
                )

                // Horizontal Items
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false
                ) {
                    items(5) {
                        // Poster Cover Skeleton
                        LoadingSkeleton(
                            modifier = Modifier
                                .width(110.dp)
                                .height(160.dp)
                        )
                    }
                }
            }
        }
    }
}
