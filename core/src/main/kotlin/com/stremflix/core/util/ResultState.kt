package com.stremflix.core.util

sealed interface ResultState<out T> {
    data object Loading : ResultState<Nothing>
    data class Success<T>(val value: T) : ResultState<T>
    data class Error(val throwable: Throwable) : ResultState<Nothing>
}
