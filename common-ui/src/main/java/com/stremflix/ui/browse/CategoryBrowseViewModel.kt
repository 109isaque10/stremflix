package com.stremflix.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.mapper.toDomainItem
import com.stremflix.data.remote.TmdbApi
import com.stremflix.data.util.TmdbGenres
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CategoryBrowseUiState {
    object Loading : CategoryBrowseUiState()
    data class Success(val items: List<com.stremflix.data.model.ContentItem>) : CategoryBrowseUiState()
    data class Error(val message: String) : CategoryBrowseUiState()
}

@HiltViewModel
class CategoryBrowseViewModel @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    val categories = TmdbGenres.MOVIE.values.sorted()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _uiState = MutableStateFlow<CategoryBrowseUiState>(CategoryBrowseUiState.Loading)
    val uiState: StateFlow<CategoryBrowseUiState> = _uiState.asStateFlow()

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        val genreId = TmdbGenres.getGenreId(category)
        if (genreId != null) {
            loadCategoryContent(genreId)
        } else {
            _uiState.value = CategoryBrowseUiState.Error("Genre ID not found")
        }
    }

    private fun loadCategoryContent(genreId: Int) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = CategoryBrowseUiState.Loading
            try {
                // Fetch both Movies and TV Shows for the genre
                val moviesResult = tmdbApi.discoverMovies(genreId)
                val tvResult = tmdbApi.discoverTvShows(genreId)

                val movieItems = moviesResult.results!!.mapNotNull { it.toDomainItem() }
                val tvItems = tvResult.results!!.mapNotNull { it.toDomainItem() }

                // Combine, remove duplicates by ID, sort by year descending
                val combined = (movieItems + tvItems)
                    .distinctBy { it.id }
                    .sortedByDescending { it.year ?: 0 }

                _uiState.value = CategoryBrowseUiState.Success(combined)
            } catch (e: Exception) {
                _uiState.value = CategoryBrowseUiState.Error(e.message ?: "Failed to load category")
            }
        }
    }
}