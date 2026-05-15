// common-ui/src/main/java/com/stremflix/ui/search/SearchViewModel.kt

package com.stremflix.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremflix.core.domain.model.Result
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val results: List<com.stremflix.data.model.ContentItem>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val preferencesDataSource: PreferencesDataSource,  // Ensure this is injected
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // FIX: Use preferencesDataSource.recentSearches flow
    val recentSearches: StateFlow<List<String>> = preferencesDataSource.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // FIX #8: Wrap collectLatest in viewModelScope.launch
        viewModelScope.launch {
            _query
                .debounce(500)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collectLatest { query ->
                    performSearch(query)
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isBlank()) {
            _uiState.value = SearchUiState.Idle
        }
    }

    private suspend fun performSearch(query: String) {
        _uiState.value = SearchUiState.Loading
        val result = contentRepository.search(query)

        if (result is Result.Success) {
            _uiState.value = SearchUiState.Success(result.data)
            // FIX #9: Use preferencesDataSource.addRecentSearch
            preferencesDataSource.addRecentSearch(query)
        } else {
            _uiState.value = SearchUiState.Error("Search failed")
        }
    }

    fun clearRecent() {
        // FIX #10: Use preferencesDataSource.clearRecentSearches
        viewModelScope.launch(dispatchers.io) {
            preferencesDataSource.clearRecentSearches()
        }
    }
}