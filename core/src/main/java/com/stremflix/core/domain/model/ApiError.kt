package com.stremflix.core.domain.model

data class ApiError(
    val code: Int = -1,
    val message: String = "Unknown error",
    val throwable: Throwable? = null
) {
    companion object {
        fun fromThrowable(throwable: Throwable, code: Int = -1): ApiError =
            ApiError(code = code, message = throwable.localizedMessage ?: "Unknown error", throwable = throwable)

        fun networkError(message: String = "Network error"): ApiError =
            ApiError(code = -2, message = message)

        fun authError(message: String = "Authentication failed"): ApiError =
            ApiError(code = -3, message = message)

        fun notFound(message: String = "Resource not found"): ApiError =
            ApiError(code = 404, message = message)

        fun serverError(message: String = "Server error"): ApiError =
            ApiError(code = 500, message = message)
    }
}