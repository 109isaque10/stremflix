package com.stremflix.data.model

import com.stremflix.core.domain.model.IdType
import com.stremflix.core.domain.model.OmdbProvider

data class UserPreferences(
    val stremioBaseUrl: String,
    val tmdbApiKey: String,
    val traktClientId: String,
    val traktClientSecret: String,
    val omdbApiKey: String?,
    val omdbEnabled: Boolean,
    val omdbProviders: Set<OmdbProvider>,
    val defaultIdType: IdType,
    val prefetchThreshold: Float,
    val popupThreshold: Float,
    val tmdbRegion: String = "US",
    val tmdbLanguage: String = "en-US",
    val preferredAudioLanguage: String = "en",
    val preferredSubtitleLanguage: String = "en",
    val forceSubtitles: Boolean = true
)