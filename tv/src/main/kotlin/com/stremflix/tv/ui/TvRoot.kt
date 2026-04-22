package com.stremflix.tv.ui

import android.net.Uri
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import com.stremflix.common.ui.SectionTitle
import com.stremflix.common.ui.TextLegibilityGradient
import com.stremflix.core.model.MediaItem
import com.stremflix.core.model.MediaType
import com.stremflix.core.usecase.GetTrailerUseCase
import com.stremflix.core.usecase.ObserveHomeRowsUseCase
import com.stremflix.tv.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun TvRoot(viewModel: TvViewModel = hiltViewModel()) {
    var splashDone by rememberSaveable { mutableStateOf(false) }
    if (!splashDone) {
        StartupVideo(rawResId = R.raw.intro_clip, onFinished = { splashDone = true })
    } else {
        TvHomeScreen(viewModel)
    }
}

@Composable
private fun StartupVideo(@RawRes rawResId: Int, onFinished: () -> Unit) {
    val context = LocalContext.current
    val player = remember {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse("android.resource://${context.packageName}/$rawResId")
            setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
            playWhenReady = false
            volume = 1f
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
    DisposableEffect(Unit) { onDispose { player.release() } }

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
private fun TvHomeScreen(viewModel: TvViewModel) {
    val media by viewModel.media.collectAsState()
    val focusedIndex by viewModel.focusedIndex.collectAsState()
    val trailerUrl by viewModel.trailerUrl.collectAsState()
    val myListItems = remember(media) { media.shuffled().take(12) }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        media.getOrNull(focusedIndex)?.let { featured ->
            HeroSection(
                item = featured,
                trailerUrl = trailerUrl,
                onFocusActive = { viewModel.setFocusedItem(focusedIndex) }
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { SectionTitle("Home") }
            item {
                TvRow(
                    title = "Trending",
                    items = media,
                    onFocus = { idx -> viewModel.setFocusedItem(idx) }
                )
            }
            item {
                TvRow(
                    title = "My List",
                    items = myListItems,
                    onFocus = { idx -> viewModel.setFocusedItem(idx) }
                )
            }
        }
    }
}

@Composable
private fun HeroSection(item: MediaItem, trailerUrl: String?, onFocusActive: () -> Unit) {
    val context = LocalContext.current
    val player = remember(trailerUrl) {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            if (!trailerUrl.isNullOrBlank()) {
                setMediaItem(androidx.media3.common.MediaItem.fromUri(trailerUrl))
                volume = 1f
                prepare()
                playWhenReady = true
            }
        }
    }
    DisposableEffect(trailerUrl) { onDispose { player.release() } }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .focusable()
            .clickable { onFocusActive() }
    ) {
        if (trailerUrl.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(item.backdropUrl ?: item.posterUrl),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (trailerUrl.startsWith("https://www.youtube.com/embed/")) {
            androidx.compose.ui.viewinterop.AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    android.webkit.WebView(it).apply {
                        settings.javaScriptEnabled = true
                        loadUrl(trailerUrl)
                    }
                }
            )
        } else {
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

        TextLegibilityGradient(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(160.dp)
        )
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(item.title, color = Color.White)
            Text("${item.matchPercent}% Match • ${item.year.orEmpty()} • ${item.quality}", color = Color.Green)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}) { Text("Play") }
                Button(onClick = {}) { Text("My List") }
            }
        }
    }
}

@Composable
private fun TvRow(title: String, items: List<MediaItem>, onFocus: (Int) -> Unit) {
    SectionTitle(title)
    LazyRow(contentPadding = PaddingValues(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items) { item ->
            var focused by remember { mutableStateOf(false) }
            val idx = items.indexOf(item)
            Box(
                modifier = Modifier
                    .size(width = 210.dp, height = if (item.type == MediaType.SHOW) 120.dp else 300.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(if (focused) 2.dp else 0.dp, Color.White, RoundedCornerShape(10.dp))
                    .scale(if (focused) 1.06f else 1f)
                    .onFocusChanged { state ->
                        focused = state.isFocused
                        if (state.isFocused) onFocus(idx)
                    }
                    .focusable()
                    .clickable { onFocus(idx) }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(if (item.type == MediaType.SHOW) item.backdropUrl else item.posterUrl),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@HiltViewModel
class TvViewModel @Inject constructor(
    observeHomeRowsUseCase: ObserveHomeRowsUseCase,
    private val getTrailerUseCase: GetTrailerUseCase
) : ViewModel() {

    private val _media = MutableStateFlow<List<MediaItem>>(emptyList())
    val media: StateFlow<List<MediaItem>> = _media.asStateFlow()

    private val _focusedIndex = MutableStateFlow(0)
    val focusedIndex = _focusedIndex.asStateFlow()

    private val _trailerUrl = MutableStateFlow<String?>(null)
    val trailerUrl = _trailerUrl.asStateFlow()

    private val autoplayTimeoutSec = 5

    init {
        viewModelScope.launch {
            observeHomeRowsUseCase().collect { _media.value = it }
        }
    }

    fun setFocusedItem(index: Int) {
        _focusedIndex.value = index
        _trailerUrl.value = null
        val item = _media.value.getOrNull(index) ?: return
        viewModelScope.launch {
            delay(autoplayTimeoutSec * 1000L)
            if (_focusedIndex.value != index) return@launch
            val trailer = getTrailerUseCase(item.tmdbId, item.type) ?: return@launch
            _trailerUrl.value = "https://www.youtube.com/embed/${trailer.key}?autoplay=1&mute=0&controls=0&rel=0"
        }
    }
}
