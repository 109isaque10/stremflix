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

private const val FIRST_SEASON = 1

suspend fun handlePlayLogic(
    item: ContentItem,
    specificEpisode: Episode?,
    playFromBeginning: Boolean,
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
        val episodeToPlay = when {
            specificEpisode != null -> specificEpisode
            playFromBeginning -> findFirstPlayableEpisode(contentRepository, item)
            else -> determineUpNext(item, watchHistoryRepository, contentRepository)
        }

        if (episodeToPlay != null && episodeToPlay.watchProgress > 0f) {
            watchHistoryRepository.updateWatchProgress(
                item.id,
                episodeToPlay.seasonNumber,
                episodeToPlay.episodeNumber,
                episodeToPlay.watchProgress
            )
        }

        if (episodeToPlay != null) {
            fetchStreams(item, episodeToPlay, streamRepository, preferencesDataSource, streamsFlow, showDialogFlow)
        } else {
            streamsFlow.value = listOf(
                Stream("No Episode Found", "", null, "", null, null, null)
            )
        }
    }
}

suspend fun determineUpNext(
    item: ContentItem,
    historyRepo: WatchHistoryRepository,
    contentRepo: ContentRepository
): Episode? {
    val lastWatched = historyRepo.getLastWatchedEpisode(item.id)
        ?: return (contentRepo.getSeasonEpisodes(item.id, 1) as? Result.Success)?.data?.firstOrNull()

    val currentSeasonRes = contentRepo.getSeasonEpisodes(item.id, lastWatched.seasonNumber)
    val episodes = (currentSeasonRes as? Result.Success)?.data ?: emptyList()

    val targetEpisodeNumber = if (lastWatched.isInProgress) {
        lastWatched.episodeNumber
    } else {
        lastWatched.episodeNumber + 1
    }

    return episodes.find { it.episodeNumber == targetEpisodeNumber }
        ?: firstAvailableEpisode(contentRepo, item.id, lastWatched.seasonNumber + 1, FIRST_SEASON)
}

private suspend fun firstAvailableEpisode(
    contentRepo: ContentRepository,
    itemId: String,
    primarySeason: Int,
    fallbackSeason: Int
): Episode? {
    for (seasonNumber in listOf(primarySeason, fallbackSeason)) {
        val result = contentRepo.getSeasonEpisodes(itemId, seasonNumber)
        val episode = (result as? Result.Success)?.data?.firstOrNull()
        if (episode != null) {
            return episode
        }
    }

    return null
}

/**
 * Finds the earliest available episode for a series by scanning its seasons in order.
 */
private suspend fun findFirstPlayableEpisode(
    contentRepo: ContentRepository,
    item: ContentItem
): Episode? {
    val maxSeason = item.numberOfSeasons?.takeIf { it > 0 } ?: FIRST_SEASON
    for (seasonNumber in FIRST_SEASON..maxSeason) {
        val result = contentRepo.getSeasonEpisodes(item.id, seasonNumber)
        val episode = (result as? Result.Success)?.data?.firstOrNull()
        if (episode != null) {
            return episode
        }
    }
    return null
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