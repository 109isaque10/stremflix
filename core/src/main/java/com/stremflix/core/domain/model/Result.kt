package com.stremflix.core.domain.model

sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: ApiError) : Result<Nothing>()
    object Loading : Result<Nothing>()

    fun <R> map(transform: (T) -> R): Result<out R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(error)
        Loading -> Loading
    }

    fun <R> flatMap(transform: (T) -> Result<R>): Result<out R> = when (this) {
        is Success -> transform(data)
        is Error -> Error(error)
        Loading -> Loading
    }

    fun dataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getOrDefault(default: T): T = when (this) {
        is Success -> data
        else -> default
    }

    fun isError(): Boolean = this is Error
    fun isSuccess(): Boolean = this is Success
    fun isLoading(): Boolean = this is Loading
}