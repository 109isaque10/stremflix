package com.stremflix.ui.util

import com.stremflix.core.domain.model.ContentType
import com.stremflix.core.domain.model.Result
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.model.ContentItem
import com.stremflix.data.model.Episode
import com.stremflix.data.model.Stream
import com.stremflix.data.repository.ContentRepository
import com.stremflix.data.repository.StreamRepository
import com.stremflix.data.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

suspend fun handlePlayLogic(
    item: ContentItem,
    specificEpisode: Episode?,
    playFromBeggining: Boolean,
    contentRepository: ContentRepository,
    streamRepository: StreamRepository,
    watchHistoryRepository: WatchHistoryRepository,
    preferencesDataSource: PreferencesDataSource,
    streamsFlow: MutableStateFlow<List<Stream>>,
    showDialogFlow: MutableStateFlow<Boolean>,
    onEpisodeDetermined: (Episode?) -> Unit = {}
) {
    showDialogFlow.value = true
    streamsFlow.value = emptyList()

    if (item.type == ContentType.MOVIE) {
        fetchStreams(item, null, streamRepository, preferencesDataSource, streamsFlow, showDialogFlow)
    } else {
        // Use provided episode or determine the "Up Next" episode
        val episodeToPlay = specificEpisode

        episodeToPlay.let { ep ->
            if (ep != null && ep.watchProgress > 0f) {
                watchHistoryRepository.updateWatchProgress(item.id, ep.seasonNumber, ep.episodeNumber, ep.watchProgress)
            }
            fetchStreams(item, ep, streamRepository, preferencesDataSource, streamsFlow, showDialogFlow)
        } ?: run {
//            showDialogFlow.value = false // Close if no episode exists
            streamsFlow.value = listOf(
                Stream(
                    "No Episode Found", "", null, "", null, null, null
                )
            )
        }
    }
}

suspend fun determineUpNext(
    item: ContentItem,
    historyRepo: WatchHistoryRepository,
    contentRepo: ContentRepository
): Episode? {
    val lastWatched = historyRepo.getLastWatchedEpisode(item.id) ?:
    return (contentRepo.getSeasonEpisodes(item.id, 1) as? Result.Success)?.data?.firstOrNull()

    val currentSeasonRes = contentRepo.getSeasonEpisodes(item.id, lastWatched.seasonNumber)
    val episodes = (currentSeasonRes as? Result.Success)?.data ?: emptyList()

    return if (lastWatched.isInProgress) {
        // Resume incomplete episode
        episodes.find { it.episodeNumber == lastWatched.episodeNumber }
    } else {
        // Suggest next episode
        episodes.find { it.episodeNumber == lastWatched.episodeNumber + 1 }
            ?: (contentRepo.getSeasonEpisodes(item.id, lastWatched.seasonNumber + 1) as? Result.Success)
                ?.data?.firstOrNull()
    }
}

private suspend fun fetchStreams(
    item: ContentItem,
    episode: Episode?,
    streamRepository: StreamRepository,
    preferencesDataSource: PreferencesDataSource,
    streamsFlow: MutableStateFlow<List<Stream>>,
    showDialogFlow: MutableStateFlow<Boolean>
) {
    val prefs = preferencesDataSource.preferencesFlow.first()
    val result = streamRepository.getStreams(
        contentId = item.id,
        contentType = if (item.type == ContentType.MOVIE) "movie" else "series",
        idType = prefs.defaultIdType,
        externalIds = item.externalIds,
        season = episode?.seasonNumber,
        episode = episode?.episodeNumber
    )
    if (result is Result.Success && result.data.isNotEmpty()) {
        streamsFlow.value = result.data
    } else {
        // Fix: Stop the infinite loading spinner if no streams are found
//        showDialogFlow.value = false
        streamsFlow.value = listOf(
            Stream(
                "No Streams Found\nID: ${item.id} S${episode?.seasonNumber}E${episode?.episodeNumber}", "", null, "", null, null, null
            )
        )
    }
}