package com.stremflix.data.remote.dto.tmdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbPagedResponse(
    val page: Int? = 1,  // Make optional with default
    val results: List<TmdbContentDto>? = emptyList(),  // Make optional
    @SerialName("total_pages") val totalPages: Int? = 0,  // Make optional
    @SerialName("total_results") val totalResults: Int? = 0  // Make optional
)

@Serializable
data class TmdbContentDto(
    val id: Int,
    val title: String?,
    val name: String?,
    @SerialName("original_title") val originalTitle: String?,
    @SerialName("original_name") val originalName: String?,
    val overview: String?,
    val popularity: Float?,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("release_date") val releaseDate: String?,
    @SerialName("first_air_date") val firstAirDate: String?,
    @SerialName("vote_average") val voteAverage: Float?,
    val genre_ids: List<Int>?,
    @SerialName("media_type") val mediaType: String? = null // Only in trending
) {
    val effectiveTitle: String get() = title ?: name ?: ""
    val effectiveDate: String get() = releaseDate ?: firstAirDate ?: ""
}

@Serializable
data class TmdbDetailsDto(
    val id: Int,
    val title: String?,
    val name: String?,
    val overview: String?,
    val popularity: Float?,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("release_date") val releaseDate: String?,
    @SerialName("first_air_date") val firstAirDate: String?,
    @SerialName("vote_average") val voteAverage: Float?,
    val runtime: Int?,
    @SerialName("episode_run_time") val episodeRunTime: List<Int>?,
    val genres: List<GenreDto>?,
    val credits: CreditsDto?,
    @SerialName("external_ids") val externalIds: ExternalIdsDto?,
    val videos: VideosResponseDto?,

    @SerialName("release_dates") val releaseDates: ReleaseDatesResponse? = null,  // ADD THIS
    @SerialName("content_ratings") val contentRatings: ContentRatingsResponse? = null,  // ADD THIS
    @SerialName("number_of_seasons") val numberOfSeasons: Int? = null
) {
    val effectiveTitle: String get() = title ?: name ?: ""
    val effectiveDate: String get() = releaseDate ?: firstAirDate ?: ""
}

@Serializable
data class ReleaseDatesResponse(
    val results: List<ReleaseDateResult>
)

@Serializable
data class ReleaseDateResult(
    val iso_3166_1: String, // Country code
    val release_dates: List<ReleaseDateItem>
)

@Serializable
data class ReleaseDateItem(
    val certification: String?,
    val type: Int // 1: Premiere, 2: Theatrical, 3: TV, 4: Digital, 5: Physical
)

@Serializable
data class ContentRatingsResponse(
    val results: List<ContentRatingResult>
)

@Serializable
data class ContentRatingResult(
    val iso_3166_1: String,
    val rating: String
)
@Serializable
data class GenreDto(
    val id: Int,
    val name: String
)

@Serializable
data class CreditsDto(
    val cast: List<CastDto>
)

@Serializable
data class CastDto(
    val id: Int,
    val name: String,
    val character: String?,
    @SerialName("profile_path") val profilePath: String?
)

@Serializable
data class ExternalIdsDto(
    @SerialName("imdb_id") val imdbId: String?,
    @SerialName("tvdb_id") val tvdbId: Int?
)

@Serializable
data class VideosResponseDto(
    val results: List<VideoDto>
)

@Serializable
data class VideoDto(
    val id: String,
    val key: String,
    val name: String?,
    val type: String, // Trailer, Teaser, etc.
    val site: String // YouTube
)