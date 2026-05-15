package com.stremflix.ui.mylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremflix.core.domain.model.Result
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.model.ContentItem
import com.stremflix.data.repository.MyListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MyListUiState {
    object Loading : MyListUiState()
    data class Success(val items: List<ContentItem>) : MyListUiState()
    data class Error(val message: String) : MyListUiState()
    object Empty : MyListUiState()
}

@HiltViewModel
class MyListViewModel @Inject constructor(
    private val myListRepository: MyListRepository,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyListUiState>(MyListUiState.Loading)
    val uiState: StateFlow<MyListUiState> = _uiState.asStateFlow()

    init {
        loadMyList()
    }

    fun loadMyList() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = MyListUiState.Loading

            try {
                myListRepository.getMyListItems().collect { items ->
                    _uiState.value = if (items.isEmpty()) {
                        MyListUiState.Empty
                    } else {
                        MyListUiState.Success(items)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MyListUiState.Error(e.message ?: "Failed to load My List")
            }
        }
    }

    fun removeFromList(item: ContentItem) {
        viewModelScope.launch(dispatchers.io) {
            myListRepository.removeFromMyList(item.id)
        }
    }

    fun syncFromTrakt() {
        viewModelScope.launch(dispatchers.io) {
            try {
                myListRepository.syncFromTrakt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}