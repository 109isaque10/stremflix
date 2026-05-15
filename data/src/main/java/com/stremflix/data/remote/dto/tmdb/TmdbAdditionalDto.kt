package com.stremflix.data.remote.dto.tmdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbSeasonDetailsDto(
    val id: Int,
    val name: String?,
    @SerialName("season_number") val seasonNumber: Int,
    val episodes: List<TmdbEpisodeDto> = emptyList()
)

@Serializable
data class TmdbEpisodeDto(
    val id: Int,
    val name: String?,
    val overview: String?,
    @SerialName("episode_number") val episodeNumber: Int,
    @SerialName("still_path") val stillPath: String?,
    val runtime: Int?,
    @SerialName("air_date") val airDate: String?
)

@Serializable
data class TmdbImageDto(
    @SerialName("file_path") val filePath: String,
    val width: Int,
    val height: Int,
    @SerialName("aspect_ratio") val aspectRatio: Float
)