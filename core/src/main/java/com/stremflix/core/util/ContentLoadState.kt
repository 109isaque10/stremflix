package com.stremflix.core.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class ContentLoadState {
    object NotStarted : ContentLoadState()
    object Loading : ContentLoadState()
    object Loaded : ContentLoadState()
    data class Error(val message: String) : ContentLoadState()
}

@Singleton
class ContentLoadManager @Inject constructor() {
    private val _state = MutableStateFlow<ContentLoadState>(ContentLoadState.NotStarted)
    val state: StateFlow<ContentLoadState> = _state.asStateFlow()

    fun setLoading() { _state.value = ContentLoadState.Loading }
    fun setLoaded() { _state.value = ContentLoadState.Loaded }
    fun setError(message: String) { _state.value = ContentLoadState.Error(message) }
    fun reset() { _state.value = ContentLoadState.NotStarted }
}