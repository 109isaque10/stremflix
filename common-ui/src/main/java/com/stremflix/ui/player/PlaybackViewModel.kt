package com.stremflix.ui.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.stremflix.core.domain.model.ContentType
import com.stremflix.core.domain.model.IdType
import com.stremflix.core.domain.model.Result
import com.stremflix.core.util.AppDispatchers
import com.stremflix.core.util.PlaybackConfig
import com.stremflix.data.local.dao.WatchHistoryDao
import com.stremflix.data.local.entity.WatchHistoryEntity
import com.stremflix.data.manager.TraktOAuthManager
import com.stremflix.data.model.ContentItem
import com.stremflix.data.model.Episode
import com.stremflix.data.model.Stream
import com.stremflix.data.remote.ImdbDevApi
import com.stremflix.data.remote.dto.trakt.EpisodeInfo
import com.stremflix.data.remote.dto.trakt.MovieInfo
import com.stremflix.data.remote.dto.trakt.TraktIds
import com.stremflix.data.remote.dto.trakt.TraktScrobbleItem
import com.stremflix.data.repository.ContentRepository
import com.stremflix.data.repository.StreamRepository
import com.stremflix.data.repository.TraktRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class UpNextInfo(
    val title: String,
    val thumbnailUrl: String?,
    val synopsis: String?,
    val seasonEpisode: String?
)



sealed class PlaybackUiState {
    object Idle : PlaybackUiState()
    object Buffering : PlaybackUiState()
    object Playing : PlaybackUiState()
    object Paused : PlaybackUiState()
    data class Error(val message: String) : PlaybackUiState()
}

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val streamRepository: StreamRepository,
    private val contentRepository: ContentRepository,
    private val watchHistoryDao: WatchHistoryDao,
    private val traktRepository: TraktRepository,
    private val imdbDevApi: ImdbDevApi,
    private val traktOAuthManager: TraktOAuthManager,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _advisories = MutableStateFlow<List<String>>(emptyList())
    val advisories = _advisories.asStateFlow()

    private val _contentRating = MutableStateFlow<String?>(null)
    val contentRating = _contentRating.asStateFlow()

    private var _player: ExoPlayer? = null
    val player: ExoPlayer
        get() {
            if (_player == null) {
                _player = ExoPlayer.Builder(context).build().apply {
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            when (state) {
                                Player.STATE_BUFFERING -> _uiState.value = PlaybackUiState.Buffering
                                Player.STATE_READY -> _uiState.value = if (playWhenReady) PlaybackUiState.Playing else PlaybackUiState.Paused
                                Player.STATE_ENDED -> handlePlaybackEnded()
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            if (isPlaying) _uiState.value = PlaybackUiState.Playing
                            else if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_BUFFERING) {
                                _uiState.value = PlaybackUiState.Paused
                            }
                        }
                    })
                }
            }
            return _player!!
        }

    private val _uiState = MutableStateFlow<PlaybackUiState>(PlaybackUiState.Idle)
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _upNextInfo = MutableStateFlow<UpNextInfo?>(null)
    val upNextInfo: StateFlow<UpNextInfo?> = _upNextInfo.asStateFlow()

    private val _showUpNextModal = MutableStateFlow(false)
    val showUpNextModal: StateFlow<Boolean> = _showUpNextModal.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(0)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()
    private val _showControls = MutableStateFlow(true)
    val showControls: StateFlow<Boolean> = _showControls.asStateFlow()

    private val _showInfoOverlay = MutableStateFlow(false)
    val showInfoOverlay: StateFlow<Boolean> = _showInfoOverlay.asStateFlow()
    private var controlsHideJob: Job? = null
    private var infoOverlayJob: Job? = null

    private var countdownJob: Job? = null
    private var positionUpdateJob: Job? = null

    // Series tracking
    private var currentContentId: String? = null
    private var contentType: ContentType? = null
    private var currentSeason: Int? = null
    private var currentEpisode: Int? = null
    private var nextEpisodeMeta: Episode? = null
    private var currentContentItem: ContentItem? = null
    private var lastScrobblePosition: Long = 0L

    fun initializePlayback(
        streamUrl: String,
        contentId: String,
        contentType: ContentType,
        season: Int? = null,
        episode: Int? = null
    ) {
        this.currentContentId = contentId
        this.contentType = contentType
        this.currentSeason = season
        this.currentEpisode = episode

        // Fetch metadata for Trakt Sync
        viewModelScope.launch(dispatchers.main) {
            val result = contentRepository.getDetails(contentId, contentType)
            if (result is Result.Success) {
                currentContentItem = result.data
            }
            val mediaItem = MediaItem.fromUri(streamUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
            // Start position updates
            startProgressTracking()

            fetchContentRating(contentId, contentType)

            // Start controls hide timer
            startControlsHideTimer()
        }

        startProgressTracking()
    }

    fun toggleControlsVisibility() {
        viewModelScope.launch {
            _showControls.value = !_showControls.value
            if (_showControls.value) {
                startControlsHideTimer()
            } else {
                controlsHideJob?.cancel()
            }
        }
    }

    private fun startControlsHideTimer() {
        controlsHideJob?.cancel()
        controlsHideJob = viewModelScope.launch {
            delay(3000) // Hide after 3 seconds
            _showControls.value = false
        }
    }

    fun showInfoOverlay() {
        viewModelScope.launch {
            _showInfoOverlay.value = true
        }
    }

    fun dismissInfoOverlay() {
        viewModelScope.launch {
            _showInfoOverlay.value = false
        }
    }

    private fun startProgressTracking() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch(dispatchers.main) {
            while (true) {
                delay(1000)
                val position = player.currentPosition
                val duration = player.duration

                _currentPosition.value = position
                _duration.value = duration
                _isPlaying.value = player.isPlaying

                if (duration > 0) {
                    updateWatchHistory(position, duration)
                }

                // Show info overlay after 15s of pause
                if (!_isPlaying.value && position > 0) {
                    startInfoOverlayTimer()
                } else {
                    infoOverlayJob?.cancel()
                }
            }
        }
    }

    private fun startInfoOverlayTimer() {
        infoOverlayJob?.cancel()
        infoOverlayJob = viewModelScope.launch {
            delay(15000) // 15 seconds
            if (!_isPlaying.value) {
                showInfoOverlay()
            }
        }
    }

    private suspend fun updateWatchHistory(position: Long, duration: Long) {
        if (currentContentId == null || duration == 0L) return

        val progress = position.toFloat() / duration.toFloat()

        // Update local watch history
        currentContentId?.let { id ->
            val existing = watchHistoryDao.getEpisode(
                id,
                currentSeason ?: 0,
                currentEpisode ?: 0
            )

            val historyEntity = existing?.copy(
                watchProgress = progress,
                lastWatchedAt = System.currentTimeMillis(),
                watched = progress >= 0.9f
            ) ?: WatchHistoryEntity(
                episodeId = "${id}_S${currentSeason ?: 0}E${currentEpisode ?: 0}",
                seriesId = id,
                seasonNumber = currentSeason ?: 0,
                episodeNumber = currentEpisode ?: 0,
                watched = progress >= 0.9f,
                watchProgress = progress,
                lastWatchedAt = System.currentTimeMillis(),
                title = "",
                synopsis = null
            )

            watchHistoryDao.insert(historyEntity)
        }

        // Scrobble to Trakt
        scrobbleToTrakt(position, duration, progress)

        // Check Thresholds
        if (progress >= PlaybackConfig.POPUP_THRESHOLD && !_showUpNextModal.value) {
            loadNextEpisodeInfo()
            _showUpNextModal.value = true
            startCountdown()
        } else if (progress >= PlaybackConfig.PREFETCH_THRESHOLD && nextEpisodeMeta != null && player.nextMediaItemIndex == C.INDEX_UNSET) {
            prefetchNextStream()
        }
    }

    private suspend fun scrobbleToTrakt(position: Long, duration: Long, progress: Float) {
        if (!traktOAuthManager.isAuthenticated()) return

        // Only scrobble every 5 minutes or at significant milestones
        if (position - lastScrobblePosition < 300000 && progress < 0.8f) return

        currentContentId?.let { id ->
            try {
                val scrobbleItem = TraktScrobbleItem(
                    progress = progress * 100f,
                    episode = if (currentSeason != null) EpisodeInfo(TraktIds(
                        id.toInt(),
                        slug = null,
                        tvdb = null,
                        imdb = null,
                        tmdb = null,
                        tvrage = null
                    )) else null,
                    movie = if (currentSeason == null) MovieInfo(TraktIds(
                        id.toInt(),
                        slug = null,
                        tvdb = null,
                        imdb = null,
                        tmdb = null,
                        tvrage = null
                    )) else null
                )

                if (progress >= 0.9f) {
                    traktRepository.scrobbleStop(scrobbleItem)
                } else {
                    traktRepository.scrobblePause(scrobbleItem)
                }

                lastScrobblePosition = position
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun loadNextEpisodeInfo() {
        if (contentType != ContentType.SERIES || currentSeason == null || currentEpisode == null) return

        val result = contentRepository.getSeasonEpisodes(currentContentId!!, currentSeason!!)
        if (result is Result.Success) {
            nextEpisodeMeta = result.data.find { it.episodeNumber == currentEpisode!! + 1 }
            nextEpisodeMeta?.let { nextEp ->
                _upNextInfo.value = UpNextInfo(
                    title = nextEp.title,
                    thumbnailUrl = nextEp.thumbnailUrl,
                    synopsis = nextEp.synopsis,
                    seasonEpisode = "S${currentSeason.toString().padStart(2, '0')}E${nextEp.episodeNumber.toString().padStart(2, '0')}"
                )
            }
        }
    }

    private suspend fun prefetchNextStream() {
        val nextEp = nextEpisodeMeta ?: return

        val streamResult = streamRepository.getStreams(
            contentId = nextEp.seriesId,
            contentType = "series",
            idType = IdType.IMDB,
            episode = nextEp.episodeNumber,
            season = nextEp.seasonNumber
        )

        if (streamResult is Result.Success && streamResult.data.isNotEmpty()) {
            val nextStream: Stream = streamResult.data.first()
            val nextMediaItem = MediaItem.fromUri(nextStream.url)
            player.addMediaItem(nextMediaItem)
        }
    }

    fun onPlayPause() {
        viewModelScope.launch {
            withContext(dispatchers.main) {
                if (player.isPlaying) player.pause() else player.play()
            }
        }
    }

    fun onSeekTo(position: Long) {
        viewModelScope.launch {
            withContext(dispatchers.main) {
                player.seekTo(position.coerceIn(0L, _duration.value))
            }
        }
    }

    fun skipForward(milliseconds: Long) {
        viewModelScope.launch(dispatchers.main) {
            val newPosition = (_currentPosition.value + milliseconds).coerceAtMost(_duration.value)
            player.seekTo(newPosition)
        }
    }

    fun skipBackward(milliseconds: Long) {
        viewModelScope.launch(dispatchers.main) {
            val newPosition = (_currentPosition.value - milliseconds).coerceAtLeast(0L)
            player.seekTo(newPosition)
        }
    }

    fun setContentInfo(contentId: String, season: Int?, episode: Int?) {
        this.currentContentId = contentId
        this.currentSeason = season
        this.currentEpisode = episode
    }

    fun onUpNextDismiss() {
        _showUpNextModal.value = false
        countdownJob?.cancel()
    }

    fun playNext() {
        _showUpNextModal.value = false
        countdownJob?.cancel()

        if (player.nextMediaItemIndex != C.INDEX_UNSET) {
            player.seekToNextMediaItem()
            player.play()

            // Update episode tracking
            currentEpisode = (currentEpisode ?: 0) + 1
        } else {
            // Fallback: restart or finish
            player.seekTo(0)
            player.play()
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in 10 downTo 0) {
                _countdownSeconds.value = i
                delay(1000)
                if (!_showUpNextModal.value) break
            }
            if (_showUpNextModal.value) {
                playNext()
            }
        }
    }

    fun fetchContentRating(contentId: String, type: ContentType) {
        viewModelScope.launch(dispatchers.io) {
            try {
                // Get the IMDB ID from our database/TMDB
                val details = (contentRepository.getDetails(contentId, type) as? Result.Success)?.data
                val imdbId = details?.externalIds?.imdbId
                _contentRating.value = details?.contentRating // e.g., "TV-MA" or "R"

                if (!imdbId.isNullOrEmpty()) {
                    val formattedImdbId = if (imdbId.startsWith("tt")) imdbId else "tt$imdbId"

                    val reasons = imdbDevApi.getParentsGuide(formattedImdbId)

                    if (reasons != null) {
                        _advisories.value = reasons
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handlePlaybackEnded() {
        if (player.nextMediaItemIndex != C.INDEX_UNSET) {
            player.seekToNextMediaItem()
            player.play()
        } else {
            _uiState.value = PlaybackUiState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            withContext(dispatchers.main) {
                _player?.release()
                _player = null
            }
        }
        positionUpdateJob?.cancel()
        countdownJob?.cancel()
        controlsHideJob?.cancel()
        infoOverlayJob?.cancel()
    }
}