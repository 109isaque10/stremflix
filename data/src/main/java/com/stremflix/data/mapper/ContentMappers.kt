package com.stremflix.data.mapper

import com.stremflix.core.domain.model.ContentType
import com.stremflix.core.util.ApiEndpoints
import com.stremflix.data.local.Converters
import com.stremflix.data.local.entity.ContentEntity
import com.stremflix.data.local.entity.WatchHistoryEntity
import com.stremflix.data.model.ContentItem
import com.stremflix.data.model.Episode
import com.stremflix.data.model.ExternalIds
import com.stremflix.data.model.WatchHistory
import com.stremflix.data.remote.dto.tmdb.TmdbContentDto
import com.stremflix.data.remote.dto.tmdb.TmdbDetailsDto
import com.stremflix.data.remote.dto.tmdb.TmdbEpisodeDto
import com.stremflix.data.remote.dto.trakt.TraktContentDto
import com.stremflix.data.remote.dto.trakt.TraktIdsDto
import com.stremflix.data.remote.dto.trakt.TraktMovie
import com.stremflix.data.remote.dto.trakt.TraktShow
import kotlinx.datetime.LocalDate

// === TMDB Content DTO ===
fun TmdbContentDto.toDomainItem(): ContentItem? {
    if (mediaType == null && title == null && name == null) return null

    val type = when (mediaType) {
        "movie" -> ContentType.MOVIE
        "tv" -> ContentType.SERIES
        else -> if (title != null) ContentType.MOVIE else ContentType.SERIES
    }

    val posterUrl = posterPath?.let { "${ApiEndpoints.TMDB_IMAGE_BASE}w500$it" }
    val backdropUrl = backdropPath?.let { "${ApiEndpoints.TMDB_IMAGE_BASE}original$it" }
    val releaseDate = effectiveDate.takeIf { it.isNotEmpty() }?.let {
        try { LocalDate.parse(it) } catch (e: Exception) { null }
    }
    val matchScore = voteAverage?.let { (it * 10).toInt().coerceIn(0, 100) }

    return ContentItem(
        id = id.toString(),
        type = type,
        title = effectiveTitle,
        year = releaseDate?.year,
        popularity = popularity,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        rating = voteAverage,
        contentRating = null, // Will be filled from details endpoint
        synopsis = overview,
        genres = emptyList(),
        cast = emptyList(),
        runtime = null,
        matchScore = matchScore,
        releaseDate = releaseDate,
        externalIds = ExternalIds(
            imdbId = null,
            tmdbId = id,
            traktId = null,
            tvdbId = null
        ),
        lastWatched = null,
        watchProgress = 0f
    )
}

// === TMDB Details DTO ===
fun TmdbDetailsDto.toDomainItem(type: ContentType, region: String = "US"): ContentItem {
    val posterUrl = posterPath?.let { "${ApiEndpoints.TMDB_IMAGE_BASE}w500$it" }
    val backdropUrl = backdropPath?.let { "${ApiEndpoints.TMDB_IMAGE_BASE}original$it" }
    val titleLogoUrl = images?.logos?.firstOrNull()?.filePath?.let { "${ApiEndpoints.TMDB_IMAGE_BASE}w500$it" }

    val releaseDate = effectiveDate.takeIf { it.isNotEmpty() }?.let {
        try { LocalDate.parse(it) } catch (e: Exception) { null }
    }
    val castList = credits?.cast?.map { it.name } ?: emptyList()
    val genreList = genres?.map { it.name } ?: emptyList()
    val matchScore = voteAverage?.let { (it * 10).toInt().coerceIn(0, 100) }

    val contentRating = when (type) {
        ContentType.MOVIE -> extractMovieCertification(region)
        ContentType.SERIES -> extractTvCertification(region)
    }

    val trailerId = videos?.results?.firstOrNull {
        it.type.equals("Trailer", ignoreCase = true) && it.site.equals(
            "YouTube",
            ignoreCase = true
        )
    }?.key

    return ContentItem(
        id = id.toString(),
        type = type,
        title = effectiveTitle,
        year = releaseDate?.year,
        popularity = popularity,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        titleLogoUrl = titleLogoUrl,
        rating = voteAverage,
        contentRating = contentRating,
        synopsis = overview,
        genres = genreList,
        cast = castList,
        runtime = runtime,
        videos = videos?.results,
        trailerId = trailerId,
        matchScore = matchScore,
        releaseDate = releaseDate,
        externalIds = ExternalIds(
            imdbId = externalIds?.imdbId,
            tmdbId = id,
            traktId = null,
            tvdbId = externalIds?.tvdbId
        ),
        lastWatched = null,
        watchProgress = 0f,
        numberOfSeasons = numberOfSeasons
    )
}

private fun TmdbDetailsDto.extractMovieCertification(region: String): String? {
    return releaseDates?.results
        ?.find { it.iso_3166_1.equals(region, ignoreCase = true) }
        ?.release_dates
        ?.filter { it.type == 3 || it.type == 4 }
        ?.maxByOrNull { it.type }
        ?.certification
        ?: releaseDates?.results
            ?.find { it.iso_3166_1.equals("US", ignoreCase = true) }
            ?.release_dates
            ?.maxByOrNull { it.type }
            ?.certification
}

private fun TmdbDetailsDto.extractTvCertification(region: String): String? {
    return contentRatings?.results
        ?.find { it.iso_3166_1.equals(region, ignoreCase = true) }
        ?.rating
        ?: contentRatings?.results
            ?.find { it.iso_3166_1.equals("US", ignoreCase = true) }
            ?.rating
}

// === Episode Mapper ===
fun TmdbEpisodeDto.toEpisode(seriesId: String, seasonNumber: Int): Episode {
    return Episode(
        seriesId = seriesId,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        title = name ?: "Episode $episodeNumber",
        synopsis = overview,
        thumbnailUrl = stillPath?.let { "${ApiEndpoints.TMDB_IMAGE_BASE}w300$it" },
        runtime = runtime,
        releaseDate = airDate?.let { try { LocalDate.parse(it) } catch (e: Exception) { null } },
        streamUrl = null,
        watched = false,
        watchProgress = 0f,
        videoUrl = null
    )
}

// === Trakt DTO ===
fun TraktContentDto.toDomainItem(): ContentItem {
    val movie = movie
    val show = show
    val type = if (movie != null) ContentType.MOVIE else ContentType.SERIES
    val title = movie?.title ?: show?.title ?: ""
    val ids = movie?.ids ?: show?.ids ?: TraktIdsDto(trakt = 0, slug = null, imdb = null, tmdb = null, tvdb = null)

    return ContentItem(
        id = ids.tmdb?.toString() ?: ids.trakt.toString(),
        type = type,
        title = title,
        year = movie?.year ?: show?.year,
        popularity = null,
        posterUrl = null,
        backdropUrl = null,
        rating = null,
        contentRating = null,
        synopsis = movie?.overview ?: show?.overview,
        genres = emptyList(),
        cast = emptyList(),
        runtime = movie?.duration,
        matchScore = null,
        releaseDate = null,
        externalIds = ExternalIds(
            imdbId = ids.imdb,
            tmdbId = ids.tmdb,
            traktId = ids.trakt,
            tvdbId = ids.tvdb
        ),
        lastWatched = null,
        watchProgress = 0f
    )
}

// === Entity Mappers ===
fun ContentItem.toEntity(): ContentEntity {
    return ContentEntity(
        id = id,
        type = type,
        title = title,
        year = year,
        popularity = popularity,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        rating = rating,
        contentRating = contentRating,
        synopsis = synopsis,
        genresJson = Converters.toStringList(genres),
        castJson = Converters.toStringList(cast),
        runtime = runtime,
        matchScore = matchScore,
        releaseDate = releaseDate?.toString(),
        imdbId = externalIds.imdbId,
        tmdbId = externalIds.tmdbId,
        traktId = externalIds.traktId,
        tvdbId = externalIds.tvdbId,
        lastWatched = lastWatched,
        watchProgress = watchProgress,
    )
}

fun ContentEntity.toDomainItem(): ContentItem {
    return ContentItem(
        id = id,
        type = type,
        title = title,
        year = year,
        popularity = popularity,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        rating = rating,
        contentRating = contentRating,
        synopsis = synopsis,
        genres = Converters.fromStringList(genresJson),
        cast = Converters.fromStringList(castJson),
        runtime = runtime,
        matchScore = matchScore,
        releaseDate = releaseDate?.let { try { LocalDate.parse(it) } catch (e: Exception) { null } },
        externalIds = ExternalIds(imdbId, tmdbId, traktId, tvdbId),
        lastWatched = lastWatched,
        watchProgress = watchProgress
    )
}

fun WatchHistoryEntity.toDomain(): WatchHistory {
    return WatchHistory(
        episodeId = episodeId,
        seriesId = seriesId,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        watched = watched,
        watchProgress = watchProgress,
        lastWatchedAt = lastWatchedAt,
        title = title,
        synopsis = synopsis
    )
}

fun WatchHistory.toEntity(): WatchHistoryEntity {
    return WatchHistoryEntity(
        episodeId = episodeId,
        seriesId = seriesId,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        watched = watched,
        watchProgress = watchProgress,
        lastWatchedAt = lastWatchedAt,
        title = title,
        synopsis = synopsis
    )
}

fun TraktMovie.toDomainItem(): ContentItem {
    return ContentItem(
        id = ids?.tmdb?.toString() ?: ids?.trakt?.toString() ?: "",
        type = ContentType.MOVIE,
        title = title ?: "",
        year = year,
        popularity = null,
        posterUrl = null,
        backdropUrl = null,
        rating = ratings?.percentage?.toDouble()?.div(10.0)?.toFloat(),
        contentRating = certification,
        synopsis = overview,
        genres = genres ?: emptyList(),
        cast = emptyList(),
        runtime = runtime,
        matchScore = null,
        releaseDate = null,
        externalIds = ExternalIds(
            imdbId = ids?.imdb,
            tmdbId = ids?.tmdb,
            traktId = ids?.trakt,
            tvdbId = ids?.tvdb
        ),
        lastWatched = null,
        watchProgress = 0f
    )
}

// Change receiver type from TraktShowDto to TraktShow
fun TraktShow.toDomainItem(): ContentItem {
    return ContentItem(
        id = ids?.tmdb?.toString() ?: ids?.trakt?.toString() ?: "",
        type = ContentType.SERIES,
        title = title ?: "",
        year = year,
        popularity = null,
        posterUrl = null,
        backdropUrl = null,
        rating = ratings?.percentage?.toDouble()?.div(10.0)?.toFloat(),
        contentRating = certification,
        synopsis = overview,
        genres = genres ?: emptyList(),
        cast = emptyList(),
        runtime = null,
        matchScore = null,
        releaseDate = null,
        externalIds = ExternalIds(
            imdbId = ids?.imdb,
            tmdbId = ids?.tmdb,
            traktId = ids?.trakt,
            tvdbId = ids?.tvdb
        ),
        lastWatched = null,
        watchProgress = 0f
    )
}