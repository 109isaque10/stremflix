package com.stremflix.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OmdbProvider {
    @SerialName("imdb")
    IMDB,

    @SerialName("rotten_tomatoes")
    ROTTEN_TOMATOES,

    @SerialName("metacritic")
    METACRITIC
}