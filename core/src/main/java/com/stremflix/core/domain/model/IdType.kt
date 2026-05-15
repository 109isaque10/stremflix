package com.stremflix.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class IdType {
    @SerialName("imdb")
    IMDB,

    @SerialName("tmdb")
    TMDB
}