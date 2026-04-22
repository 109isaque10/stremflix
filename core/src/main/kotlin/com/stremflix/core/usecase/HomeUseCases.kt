package com.stremflix.core.usecase

import com.stremflix.core.model.MediaItem
import com.stremflix.core.model.MediaType
import com.stremflix.core.model.StreamSource
import com.stremflix.core.model.Trailer
import com.stremflix.core.repository.MetadataRepository
import com.stremflix.core.repository.StreamRepository
import com.stremflix.core.repository.TraktRepository
import kotlinx.coroutines.flow.Flow

class ObserveHomeRowsUseCase(private val metadataRepository: MetadataRepository) {
    operator fun invoke(): Flow<List<MediaItem>> = metadataRepository.trending()
}

class SearchMediaUseCase(private val metadataRepository: MetadataRepository) {
    operator fun invoke(query: String, moviesOnly: Boolean? = null) = metadataRepository.search(query, moviesOnly)
}

class GetTrailerUseCase(private val metadataRepository: MetadataRepository) {
    suspend operator fun invoke(tmdbId: Int, mediaType: MediaType): Trailer? =
        metadataRepository.trailers(tmdbId, mediaType).firstOrNull()
}

class ResolveStreamUseCase(private val streamRepository: StreamRepository) {
    suspend operator fun invoke(tmdbId: Int, mediaType: MediaType): StreamSource? =
        streamRepository.resolveStreams(tmdbId, mediaType).firstOrNull()
}

class FetchTraktRowsUseCase(private val traktRepository: TraktRepository) {
    suspend operator fun invoke() = traktRepository.fetchPersonalListRows()
}
