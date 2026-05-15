package com.stremflix.data.remote.dto.trakt

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============ WATCHLIST ============

@Serializable
data class TraktWatchlistItem(
    val rank: Int,
    val id: Long,
    @SerialName("listed_at") val listedAt: Instant,
    val notes: String?,
    val type: String,
    val movie: TraktMovie?,
    val show: TraktShow?
)

// ============ MOVIES & SHOWS ============

@Serializable
data class TraktMovie(
    val title: String?,
    val year: Int?,
    val ids: TraktIds?,
    val tagline: String?,
    val overview: String?,
    val released: String?,
    val runtime: Int?,
    val country: String?,
    val homepage: String?,
    val status: String?,
    val language: String?,
    val aired_episodes: Int?,
    val ratings: TraktRatings?,
    val trailer: String?,
    val certification: String?,
    val genres: List<String>?
)

@Serializable
data class TraktShow(
    val title: String?,
    val year: Int?,
    val ids: TraktIds?,
    val overview: String?,
    val first_aired: String?,
    val aired_episodes: Int?,
    val ratings: TraktRatings?,
    val certification: String?,
    val network: String?,
    val status: String?,
    val language: String?,
    val genres: List<String>?
)

@Serializable
data class TraktIds(
    val trakt: Int,
    val slug: String?,
    val tvdb: Int?,
    val imdb: String?,
    val tmdb: Int?,
    val tvrage: String?
)

@Serializable
data class TraktRatings(
    val percentage: Int,
    val votes: Int,
    val loved: Int,
    val hated: Int
)

// ============ TRENDING ============

@Serializable
data class TraktTrendingItem(
    val watchers: Int,
    val movie: TraktMovie?,
    val show: TraktShow?
)

// ============ WATCHED ============

@Serializable
data class TraktWatchedItem(
    val plays: Int,
    val last_watched_at: Instant?,
    val movie: TraktMovie?,
    val show: TraktShow?
)

@Serializable
data class TraktWatchedShow(
    val plays: Int,
    val last_watched_at: Instant?,
    val show: TraktShow?,
    val seasons: List<TraktWatchedSeason>?
)

@Serializable
data class TraktWatchedSeason(
    val number: Int,
    val episodes: List<TraktWatchedEpisode>?
)

@Serializable
data class TraktWatchedEpisode(
    val number: Int,
    val plays: Int,
    val last_watched_at: Instant?
)

// ============ ANTICIPATED ============

@Serializable
data class TraktAnticipatedItem(
    val list_count: Int,
    val movie: TraktMovie?,
    val show: TraktShow?
)

// ============ CALENDAR ============

@Serializable
data class TraktCalendarItem(
    @SerialName("first_aired") val firstAired: Instant,
    val episode: TraktEpisodeInfo?,
    val show: TraktShow?
)

@Serializable
data class TraktEpisodeInfo(
    val season: Int,
    val number: Int,
    val title: String?,
    val ids: TraktIds?
)

// ============ HISTORY ============

@Serializable
data class TraktHistoryItem(
    val movies: List<TraktHistoryMovie>?,
    val shows: List<TraktHistoryShow>?,
    val episodes: List<TraktHistoryEpisode>?
)

@Serializable
data class TraktHistoryMovie(
    val ids: TraktIds?
)

@Serializable
data class TraktHistoryShow(
    val ids: TraktIds?,
    val seasons: List<TraktHistorySeason>?
)

@Serializable
data class TraktHistorySeason(
    val number: Int,
    val episodes: List<TraktHistoryEpisodeNumber>?
)

@Serializable
data class TraktHistoryEpisodeNumber(
    val number: Int
)

@Serializable
data class TraktHistoryEpisode(
    val ids: TraktIds?
)

// ============ SCROBBLING ============

@Serializable
data class TraktScrobbleItem(
    val progress: Float,
    val episode: EpisodeInfo?,
    val movie: MovieInfo?
)

@Serializable
data class EpisodeInfo(
    val ids: TraktIds?
)

@Serializable
data class MovieInfo(
    val ids: TraktIds?
)

// ============ USER ============

@Serializable
data class TraktUser(
    val username: String?,
    val private: Boolean?,
    val name: String?,
    val vip: Boolean?,
    val vip_ep: Boolean?,
    val ids: TraktUserIds?,
    val joined_at: Instant?,
    val location: String?,
    val about: String?,
    val gender: String?,
    val age: Int?,
    val images: TraktUserImages?
)

@Serializable
data class TraktUserIds(
    val slug: String?
)

@Serializable
data class TraktUserImages(
    val avatar: AvatarImage?
)

@Serializable
data class AvatarImage(
    val full: String?
)

@Serializable
data class TraktUserSettings(
    val user: TraktUser?,
    val account: TraktAccount?,
    val connections: TraktConnections?
)

@Serializable
data class TraktAccount(
    val timezone: String?,
    val date_format: String?,
    val time_24hr: Boolean?
)

@Serializable
data class TraktConnections(
    val twitter: Boolean?,
    val google: Boolean?,
    val tumblr: Boolean?,
    val medium: Boolean?,
    val slack: Boolean?,
    val facebook: Boolean?
)