package com.stremflix.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbPagedResponse(
    @SerialName("results") val results: List<TmdbMediaDto> = emptyList()
)

@Serializable
data class TmdbMediaDto(
    val id: Int,
    @SerialName("name") val tvName: String? = null,
    val title: String? = null,
    val overview: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("media_type") val mediaType: String? = null
)

@Serializable
data class TmdbVideosResponse(
    val results: List<TmdbVideoDto> = emptyList()
)

@Serializable
data class TmdbVideoDto(
    val key: String,
    val site: String,
    val type: String
)

@Serializable
data class TmdbCreditsResponse(
    val cast: List<TmdbCastDto> = emptyList()
)

@Serializable
data class TmdbCastDto(
    val name: String,
    val character: String? = null,
    @SerialName("profile_path") val profilePath: String? = null
)
