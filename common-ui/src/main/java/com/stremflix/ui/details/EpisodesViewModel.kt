package com.stremflix.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.stremflix.core.domain.model.ContentType
import com.stremflix.core.domain.model.Result
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.model.ContentItem
import com.stremflix.data.model.Episode
import com.stremflix.data.repository.ContentRepository
import com.stremflix.ui.navigation.AppRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val args: AppRoute.Episodes = savedStateHandle.toRoute<AppRoute.Episodes>()
    val contentId: String = args.contentId
    private val initialSeason: Int = args.season ?: 1

    private val _contentItem = MutableStateFlow<ContentItem?>(null)
    val contentItem: StateFlow<ContentItem?> = _contentItem.asStateFlow()

    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes: StateFlow<List<Episode>> = _episodes.asStateFlow()

    private val _seasons = MutableStateFlow<List<Int>>(emptyList())
    val seasons: StateFlow<List<Int>> = _seasons.asStateFlow()

    private val _currentSeason = MutableStateFlow(initialSeason)
    val currentSeason: StateFlow<Int> = _currentSeason.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch(dispatchers.io) {
            // load details
            val result = contentRepository.getDetails(contentId, ContentType.SERIES)
            if (result is Result.Success) {
                _contentItem.value = result.data
                result.data.numberOfSeasons?.let {
                    _seasons.value = (1..it).toList()
                } ?: run {
                    _seasons.value = (1..10).toList()
                }
                // load initial season episodes
                loadEpisodesForSeason(initialSeason)
            } else {
                _contentItem.value = null
                _seasons.value = emptyList()
            }
        }
    }

    private suspend fun loadEpisodesForSeason(seasonNumber: Int) {
        val res = contentRepository.getSeasonEpisodes(contentId, seasonNumber)
        if (res is Result.Success) {
            _episodes.value = res.data
            _currentSeason.value = seasonNumber
        } else {
            _episodes.value = emptyList()
        }
    }

    fun onSeasonSelected(seasonNumber: Int) {
        viewModelScope.launch(dispatchers.io) {
            _episodes.value = emptyList()
            loadEpisodesForSeason(seasonNumber)
        }
    }
}