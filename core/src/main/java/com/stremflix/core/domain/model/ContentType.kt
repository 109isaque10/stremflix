package com.stremflix.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ContentType {
    @SerialName("movie")
    MOVIE,

    @SerialName("series")
    SERIES
}