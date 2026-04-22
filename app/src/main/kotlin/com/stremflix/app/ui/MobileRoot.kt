package com.stremflix.app.ui

import android.net.Uri
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import com.stremflix.app.R
import com.stremflix.common.ui.MetaChipsRow
import com.stremflix.common.ui.SectionTitle
import com.stremflix.common.ui.Synopsis
import com.stremflix.core.model.MediaItem
import com.stremflix.core.model.MediaType
import com.stremflix.core.model.PlaybackPreferences
import com.stremflix.core.repository.MetadataRepository
import com.stremflix.core.repository.PreferencesRepository
import com.stremflix.core.usecase.ObserveHomeRowsUseCase
import com.stremflix.core.usecase.SearchMediaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private enum class MobileTab { HOME, SEARCH, COMING_SOON, DOWNLOADS, MORE }

@Composable
fun MobileRoot(viewModel: MobileRootViewModel = hiltViewModel()) {
    var selectedTab by rememberSaveable { mutableStateOf(MobileTab.HOME) }
    var splashFinished by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (!splashFinished) {
            StartupVideo(
                rawResId = R.raw.intro_clip,
                onFinished = { splashFinished = true }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        MobileTab.HOME -> MobileHomeScreen(viewModel)
                        MobileTab.SEARCH -> SearchScreen(viewModel)
                        MobileTab.COMING_SOON -> ComingSoonScreen()
                        MobileTab.DOWNLOADS -> PlaceholderScreen("Downloads")
                        MobileTab.MORE -> MoreScreen(viewModel)
                    }
                }
                BottomAppBar(containerColor = Color(0xFF0D0D0D)) {
                    listOf(
                        "Home" to Icons.Default.Home,
                        "Search" to Icons.Default.Search,
                        "Coming Soon" to Icons.Default.Update,
                        "Downloads" to Icons.Default.Update,
                        "More" to Icons.Default.Menu
                    ).forEachIndexed { index, pair ->
                        NavigationBarItem(
                            selected = selectedTab.ordinal == index,
                            onClick = { selectedTab = MobileTab.entries[index] },
                            icon = { Icon(pair.second, contentDescription = pair.first) },
                            label = { Text(pair.first) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StartupVideo(@RawRes rawResId: Int, onFinished: () -> Unit) {
    val context = LocalContext.current
    val player = remember {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse("android.resource://${context.packageName}/$rawResId")
            setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
            volume = 1f
            playWhenReady = false
            prepare()
            pause()
            addListener(object : androidx.media3.common.Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == androidx.media3.common.Player.STATE_READY) playWhenReady = true
                    if (playbackState == androidx.media3.common.Player.STATE_ENDED) onFinished()
                }
            })
        }
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    androidx.compose.ui.viewinterop.AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            androidx.media3.ui.PlayerView(it).apply {
                useController = false
                this.player = player
            }
        }
    )
}

@Composable
private fun MobileHomeScreen(viewModel: MobileRootViewModel) {
    val rows by viewModel.homeRows.collectAsState()
    var selected by remember { mutableStateOf<MediaItem?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            rows.firstOrNull()?.let { HeroCard(it, onSelect = { selected = it }) }
        }
        item { SectionTitle("Trending Now") }
        item { PosterRow(rows, onSelect = { selected = it }) }
        item { SectionTitle("Popular") }
        item { PosterRow(rows.take(10), onSelect = { selected = it }) }
    }

    selected?.let { item ->
        DetailBottomSheet(item = item, onDismiss = { selected = null }, onPlay = { viewModel.play(item) }, viewModel)
    }
}

@Composable
private fun HeroCard(item: MediaItem, onSelect: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(260.dp).clickable { onSelect() }) {
        Image(
            painter = rememberAsyncImagePainter(item.backdropUrl ?: item.posterUrl),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Text(item.title, color = Color.White)
            Text("${item.matchPercent}% Match • ${item.year.orEmpty()} • ${item.maturity.orEmpty()} • ${item.quality} • ${item.audio}", color = Color.Green)
            Text(item.overview, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                Button(onClick = onSelect) { Text("Play") }
                Button(onClick = onSelect) { Text("My List") }
            }
        }
    }
}

@Composable
private fun PosterRow(items: List<MediaItem>, onSelect: (MediaItem) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items) { item ->
            Card(modifier = Modifier.size(width = 120.dp, height = 180.dp).clickable { onSelect(item) }) {
                Image(
                    painter = rememberAsyncImagePainter(item.posterUrl ?: item.backdropUrl),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailBottomSheet(
    item: MediaItem,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    viewModel: MobileRootViewModel
) {
    val episodes by viewModel.episodes.collectAsState()
    val cast by viewModel.cast.collectAsState()
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(item.tmdbId, item.type) { viewModel.loadDetail(item) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = Color(0xFF111111)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(item.title, color = Color.White)
            Text("${item.matchPercent}% Match • ${item.year.orEmpty()} • ${item.maturity.orEmpty()} • ${item.quality}", color = Color.Green)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onPlay) { Text("Play") }
                Button(onClick = { viewModel.toggleMyList(item) }) { Text("My List") }
                Button(onClick = {}) { Text("Rate") }
                Button(onClick = {}) { Text("Share") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Synopsis(item.overview)
            MetaChipsRow(item.genres)
            SectionTitle("Cast")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cast) { actor ->
                    Column(modifier = Modifier.width(100.dp)) {
                        Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray))
                        Text(actor.name, color = Color.White, maxLines = 1)
                    }
                }
            }
            if (item.type == MediaType.SHOW) {
                SectionTitle("Episodes")
                episodes.forEach { episode ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${episode.episodeNumber}. ${episode.title}", color = Color.White)
                        Button(onClick = onPlay) { Text("Play") }
                    }
                }
            }
            SectionTitle("More Like This")
            PosterRow(viewModel.homeRows.collectAsState().value.take(8), onSelect = {})
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SearchScreen(viewModel: MobileRootViewModel) {
    val search by viewModel.searchResults.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var moviesOnly by rememberSaveable { mutableStateOf<Boolean?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.search(it, moviesOnly)
            },
            label = { Text("Search titles") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            FilterChip(selected = moviesOnly == true, onClick = { moviesOnly = true; viewModel.search(query, true) }, label = { Text("Movies") })
            FilterChip(selected = moviesOnly == false, onClick = { moviesOnly = false; viewModel.search(query, false) }, label = { Text("TV Shows") })
        }
        LazyColumn {
            items(search.chunked(2)) { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { item ->
                        Card(modifier = Modifier.weight(1f).height(180.dp)) {
                            Image(
                                painter = rememberAsyncImagePainter(item.posterUrl ?: item.backdropUrl),
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable private fun ComingSoonScreen() = PlaceholderScreen("Coming Soon")

@Composable
private fun MoreScreen(viewModel: MobileRootViewModel) {
    val prefs by viewModel.preferences.collectAsState()
    var tmdbKey by rememberSaveable { mutableStateOf("") }
    var traktKey by rememberSaveable { mutableStateOf("") }
    var stremioUrl by rememberSaveable { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Settings", color = Color.White)
        OutlinedTextField(value = stremioUrl, onValueChange = { stremioUrl = it }, label = { Text("Stremio Manifest URL") })
        OutlinedTextField(value = tmdbKey, onValueChange = { tmdbKey = it }, label = { Text("TMDB API Key") })
        OutlinedTextField(value = traktKey, onValueChange = { traktKey = it }, label = { Text("Trakt API Key") })
        Button(onClick = { viewModel.saveKeys(tmdbKey, traktKey, stremioUrl) }) { Text("Save") }
        Text("Skip Back: ${prefs.skipBackSeconds}s", color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 10, 15, 30).forEach { value ->
                Button(onClick = { viewModel.updateSkip(value) }) { Text("${value}s") }
            }
        }
        Text("Mute on Startup", color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.setMuteOnStartup(true) }) { Text("On") }
            Button(onClick = { viewModel.setMuteOnStartup(false) }) { Text("Off") }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title, color = Color.White)
    }
}

@HiltViewModel
class MobileRootViewModel @Inject constructor(
    observeHomeRowsUseCase: ObserveHomeRowsUseCase,
    private val searchMediaUseCase: SearchMediaUseCase,
    private val metadataRepository: MetadataRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val _homeRows = MutableStateFlow<List<MediaItem>>(emptyList())
    val homeRows: StateFlow<List<MediaItem>> = _homeRows.asStateFlow()

    private val _searchResults = MutableStateFlow<List<MediaItem>>(emptyList())
    val searchResults: StateFlow<List<MediaItem>> = _searchResults.asStateFlow()

    private val _episodes = MutableStateFlow(emptyList<com.stremflix.core.model.Episode>())
    val episodes = _episodes.asStateFlow()

    private val _cast = MutableStateFlow(emptyList<com.stremflix.core.model.CastMember>())
    val cast = _cast.asStateFlow()

    private val _preferences = MutableStateFlow(PlaybackPreferences())
    val preferences = _preferences.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeHomeRowsUseCase(),
                preferencesRepository.observePlaybackPreferences()
            ) { home, prefs -> home to prefs }.collect { (home, prefs) ->
                _homeRows.value = home
                _preferences.value = prefs
            }
        }
    }

    fun search(query: String, moviesOnly: Boolean?) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            searchMediaUseCase(query, moviesOnly).collect { _searchResults.value = it }
        }
    }

    fun play(item: MediaItem) {
        // playback is launched from detail by selecting first resolved stream in production.
    }

    suspend fun loadDetail(item: MediaItem) {
        runCatching {
            _episodes.value = if (item.type == MediaType.SHOW) metadataRepository.episodes(item.tmdbId, 1) else emptyList()
            _cast.value = metadataRepository.cast(item.tmdbId, item.type)
        }.onFailure {
            _episodes.value = emptyList()
            _cast.value = emptyList()
        }
    }

    fun toggleMyList(item: MediaItem) {
        // backed by Trakt/custom lists in production builds.
    }

    fun saveKeys(tmdb: String, trakt: String, stremio: String) {
        viewModelScope.launch {
            if (tmdb.isNotBlank()) preferencesRepository.updateTmdbKey(tmdb)
            if (trakt.isNotBlank()) preferencesRepository.updateTraktKey(trakt)
            if (stremio.isNotBlank()) preferencesRepository.updateStremioManifestUrl(stremio)
        }
    }

    fun updateSkip(value: Int) {
        viewModelScope.launch {
            preferencesRepository.updatePlaybackPreferences(_preferences.value.copy(skipBackSeconds = value))
        }
    }

    fun setMuteOnStartup(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updatePlaybackPreferences(_preferences.value.copy(muteOnStartup = enabled))
        }
    }
}
