package com.stremflix.data.mapper

import com.stremflix.core.model.CastMember
import com.stremflix.core.model.MediaItem
import com.stremflix.core.model.MediaType
import com.stremflix.core.model.SubtitleTrack
import com.stremflix.core.model.Trailer
import com.stremflix.data.network.dto.StremioStreamDto
import com.stremflix.data.network.dto.TmdbCastDto
import com.stremflix.data.network.dto.TmdbMediaDto
import com.stremflix.data.network.dto.TmdbVideoDto

private const val IMAGE_BASE = "https://image.tmdb.org/t/p/w780"

fun TmdbMediaDto.toMediaItem(): MediaItem {
    val type = if (mediaType == "tv" || tvName != null) MediaType.SHOW else MediaType.MOVIE
    return MediaItem(
        tmdbId = id,
        type = type,
        title = tvName ?: title.orEmpty(),
        backdropUrl = backdropPath?.let { "$IMAGE_BASE$it" },
        posterUrl = posterPath?.let { "$IMAGE_BASE$it" },
        logoUrl = backdropPath?.let { "$IMAGE_BASE$it" },
        overview = overview.orEmpty(),
        year = (firstAirDate ?: releaseDate)?.take(4),
        seasons = if (type == MediaType.SHOW) 1 else null,
        maturity = "16+",
        genres = listOf("Drama", "Action")
    )
}

fun TmdbVideoDto.toTrailer(): Trailer = Trailer(key = key, site = site, type = type)

fun TmdbCastDto.toCast(): CastMember = CastMember(
    name = name,
    character = character.orEmpty(),
    imageUrl = profilePath?.let { "$IMAGE_BASE$it" }
)

fun StremioStreamDto.toUrlAndHeaders(): Pair<String, Map<String, String>>? {
    val streamUrl = url ?: ytId?.let { "https://www.youtube.com/watch?v=$it" } ?: return null
    val headers = behaviorHints?.proxyHeaders?.request.orEmpty()
    return streamUrl to headers
}

fun StremioStreamDto.toSubtitleTracks(): List<SubtitleTrack> = subtitles.map {
    SubtitleTrack(language = it.lang, url = it.url)
}
