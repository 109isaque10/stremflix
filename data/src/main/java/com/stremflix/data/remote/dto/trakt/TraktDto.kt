package com.stremflix.data.remote.dto.trakt

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceCodeResponse(
    @SerialName("device_code") val deviceCode: String,
    @SerialName("user_code") val userCode: String,
    @SerialName("verification_url") val verificationUrl: String,
    val expires_in: Int,
    val interval: Int
)

@Serializable
data class AccessTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("scope") val scope: String?
)

@Serializable
data class TraktContentDto(
    val movie: TraktMovieDto?,
    val show: TraktShowDto?
) {
    val isMovie: Boolean get() = movie != null
    val content: Any get() = movie ?: show!!
}

@Serializable
data class TraktMovieDto(
    val title: String,
    val year: Int?,
    val ids: TraktIdsDto,
    val overview: String?,
    @SerialName("runtime") val duration: Int?,
    val tagline: String?,
    val released: String?
)

@Serializable
data class TraktShowDto(
    val title: String,
    val year: Int?,
    val ids: TraktIdsDto,
    val overview: String?,
    val aired_episodes: Int?
)

@Serializable
data class TraktIdsDto(
    val trakt: Int,
    val slug: String?,
    val imdb: String?,
    val tmdb: Int?,
    val tvdb: Int?
)

@Serializable
data class TraktHistoryDto(
    val id: Long,
    val watched_at: String,
    val action: String,
    val type: String,
    val movie: TraktMovieDto?,
    val show: TraktShowDto?,
    val episode: TraktEpisodeDto?
)

@Serializable
data class TraktEpisodeDto(
    val season: Int,
    val number: Int,
    val ids: TraktIdsDto,
    val title: String?,
    val overview: String?
)