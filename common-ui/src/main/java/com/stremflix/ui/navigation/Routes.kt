package com.stremflix.ui.navigation

import kotlinx.serialization.Serializable

sealed class AppRoute {
    @Serializable
    data object TraktAuth : AppRoute()

    @Serializable
    data object Home : AppRoute()

    @Serializable
    data object Search : AppRoute()

    @Serializable
    data object TVShows : AppRoute()

    @Serializable
    data object Movies : AppRoute()

    @Serializable
    data object MyList : AppRoute()

    @Serializable
    data object Splash : AppRoute()

    @Serializable
    data class CategoryBrowse(
        val genreName: String?
    ) : AppRoute()

    @Serializable
    data class Details(
        val contentTitle: String,
        val contentSynopsis: String?,
        val id: String,
        val type: String // "movie" or "series"
    ) : AppRoute()

    @Serializable
    data object Settings : AppRoute()

    @Serializable
    data class Episodes(
        val contentId: String,
        val season: Int? = null
    ) : AppRoute()

    @Serializable
    data class OAuthCallback(val code: String) : AppRoute()

    @Serializable
    data class PlaybackRoute(
        val streamUrl: String,
        val contentTitle: String,
        val contentSynopsis: String?,
        val contentId: String,
        val type: String,
        val playFromBeginning: Boolean = false,
        val season: Int? = null,
        val episode: Int? = null
    ) : AppRoute()
}