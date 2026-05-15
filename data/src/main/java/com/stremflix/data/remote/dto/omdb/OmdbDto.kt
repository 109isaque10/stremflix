package com.stremflix.data.remote.dto.omdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OmdbResponse(
    val Title: String?,
    val Year: String?,
    val Rated: String?,
    val Released: String?,
    val Runtime: String?,
    val Genre: String?,
    val Director: String?,
    val Writer: String?,
    val Actors: String?,
    val Plot: String?,
    val Language: String?,
    val Country: String?,
    val Awards: String?,
    @SerialName("Poster") val poster: String?,
    val Ratings: List<OmdbRatingDto>?,
    val imdbRating: String?,
    val imdbVotes: String?,
    @SerialName("imdbID") val imdbId: String?,
    val Type: String?,
    val Response: String
)

@Serializable
data class OmdbRatingDto(
    val Source: String,
    val Value: String
)