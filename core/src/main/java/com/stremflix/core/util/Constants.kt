package com.stremflix.core.util

object ApiEndpoints {
    const val STREMIO_BASE = "https://v3-cinemeta.strem.io/"
    const val TRAKT_BASE = "https://api.trakt.tv/"
    const val TMDB_BASE = "https://api.themoviedb.org/3/"
    const val OMDB_BASE = "https://www.omdbapi.com/"

    const val IMDB_BASE = "https://imdbapi.dev/"

    const val TRAKT_API_VERSION = "2"
    const val TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/"

    // Fallbacks (User must configure these in settings for production, or use these placeholders)
    const val FALLBACK_TRAKT_CLIENT_ID = "YOUR_TRAKT_CLIENT_ID"
    const val FALLBACK_TRAKT_CLIENT_SECRET = "YOUR_TRAKT_CLIENT_SECRET"
    const val FALLBACK_TRAKT_REDIRECT_URI = "com.stremflix://trakt/auth"

    const val FALLBACK_TMDB_API_KEY = "YOUR_TMDB_API_KEY"
    const val FALLBACK_STREMIO_BASE = "https://v3-cinemeta.strem.io/"

    // Mobile OAuth Redirect
    const val TRAKT_REDIRECT_URI = "com.stremflix://trakt/callback"
}

object Timeouts {
    const val CONNECTION_TIMEOUT_MS = 30_000L
    const val READ_TIMEOUT_MS = 60_000L
    const val WRITE_TIMEOUT_MS = 30_000L
}

object CacheConfig {
    const val METADATA_TTL_HOURS = 24L
    const val EXTERNAL_IDS_TTL_HOURS = 72L
    const val STREAMS_TTL_MINUTES = 60L
    const val TRENDING_TTL_HOURS = 6L
    const val SEARCH_TTL_HOURS = 1L

    const val MEMORY_CACHE_SIZE = 50
    const val DATABASE_CACHE_SIZE = 1000
}

object PlaybackConfig {
    const val PREFETCH_THRESHOLD = 0.94f
    const val POPUP_THRESHOLD = 0.97f
    const val AUTO_PLAY_DELAY_MS = 5_000L
    const val PROGRESS_SYNC_INTERVAL_MS = 30_000L
}

object UiConfig {
    const val HERO_IDLE_TIMEOUT_MS = 5_000L
    const val GRADIENT_START_ALPHA = 0f
    const val GRADIENT_END_ALPHA = 0.85f
    const val CARD_SCALE_FOCUSED = 1.05f
    const val CARD_SCALE_DEFAULT = 1f
}

object BuildKeys {
    const val STREMIO_API_KEY = "STREMIO_API_KEY"
    const val TRAKT_CLIENT_ID = "TRAKT_CLIENT_ID"
    const val TRAKT_CLIENT_SECRET = "TRAKT_CLIENT_SECRET"
    const val TMDB_API_KEY = "TMDB_API_KEY"
    const val OMDB_API_KEY = "OMDB_API_KEY"
}