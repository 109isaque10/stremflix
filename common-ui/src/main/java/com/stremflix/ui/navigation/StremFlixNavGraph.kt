package com.stremflix.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.stremflix.core.domain.model.ContentType
import com.stremflix.ui.auth.OAuthCallbackScreen
import com.stremflix.ui.auth.TraktAuthScreen
import com.stremflix.ui.browse.CategoryBrowseScreen
import com.stremflix.ui.browse.CategoryBrowseViewModel
import com.stremflix.ui.details.DetailsScreen
import com.stremflix.ui.details.EpisodesScreenRoute
import com.stremflix.ui.home.HomeScreen
import com.stremflix.ui.home.HomeViewModel
import com.stremflix.ui.player.PlaybackScreen
import com.stremflix.ui.search.SearchScreen
import com.stremflix.ui.settings.SettingsScreen
import com.stremflix.ui.splash.SplashScreen

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun StremFlixNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Any = AppRoute.Splash,
    scaffoldPadding: PaddingValues,
    isTvMode: Boolean = false
) {
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<AppRoute.TraktAuth> {
            TraktAuthScreen(
                onNavigateBack = {
                    navController.navigate(AppRoute.Home) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                },
                onSuccess = {
                    // Navigate back to Settings after successful auth
                    navController.navigate(AppRoute.Settings)
                }
            )
        }

        composable<AppRoute.Home> {
            HomeScreen(
                onNavigateToDetails = { title, synopsis, id, type ->
                    navController.navigate(AppRoute.Details(title, synopsis, id, type))
                },
                onNavigateToSettings = {
                    navController.navigate(AppRoute.Settings)
                },
                onNavigateToPlayback = { streamUrl, contentTitle, contentSynopsis, contentId, type ->
                    navController.navigate(
                        AppRoute.PlaybackRoute(streamUrl, contentTitle, contentSynopsis, contentId, type)
                    )
                },
                onNavigateToCategory = {genre -> navController.navigate(AppRoute.CategoryBrowse(genre))},
                homeViewModel = homeViewModel,
                isTvMode = isTvMode,
                filterType = "home"
            )
        }

        composable<AppRoute.Search> {
            SearchScreen(
                onNavigateToDetails = { title, synopsis, id, type ->
                    navController.navigate(AppRoute.Details(title, synopsis, id, type))
                },
                scaffoldPadding = scaffoldPadding,
                isTvMode = isTvMode
            )
        }

        composable<AppRoute.TVShows> {
            // Reuse HomeScreen filtered or a specific screen
            HomeScreen(
                onNavigateToDetails = { title, synopsis, id, type ->
                    navController.navigate(AppRoute.Details(title, synopsis, id, type))
                },
                onNavigateToSettings = {
                    navController.navigate(AppRoute.Settings)  // ADD THIS
                },
                onNavigateToPlayback = { streamUrl, contentTitle, contentSynopsis, contentId, type ->
                    // Navigate to PlaybackRoute
                    navController.navigate(
                        AppRoute.PlaybackRoute(
                            streamUrl = streamUrl,
                            contentTitle = contentTitle,
                            contentSynopsis = contentSynopsis,
                            contentId = contentId,
                            type = type
                        )
                    )
                },
                onNavigateToCategory = {navController.navigate(AppRoute.CategoryBrowse(null))},
                isTvMode = isTvMode,
                filterType = "tv",
            )
        }

        composable<AppRoute.Movies> {
            HomeScreen(
                onNavigateToDetails = { title, synopsis, id, type ->
                    navController.navigate(AppRoute.Details(title, synopsis, id, type))
                },
                onNavigateToSettings = {
                    navController.navigate(AppRoute.Settings)  // ADD THIS
                },
                onNavigateToPlayback = { streamUrl, contentTitle, contentSynopsis, contentId, type ->
                    // Navigate to PlaybackRoute
                    navController.navigate(
                        AppRoute.PlaybackRoute(
                            streamUrl = streamUrl,
                            contentTitle = contentTitle,
                            contentSynopsis = contentSynopsis,
                            contentId = contentId,
                            type = type
                        )
                    )
                },
                onNavigateToCategory = {navController.navigate(AppRoute.CategoryBrowse(null))},
                isTvMode = isTvMode,
                filterType = "movie",
            )
        }

        composable<AppRoute.MyList> {
            HomeScreen(
                onNavigateToDetails = { title, synopsis, id, type ->
                    navController.navigate(AppRoute.Details(title,synopsis, id, type))
                },
                onNavigateToSettings = {
                    navController.navigate(AppRoute.Settings)  // ADD THIS
                },
                onNavigateToPlayback = { streamUrl, contentTitle, contentSynopsis, contentId, type ->
                    // Navigate to PlaybackRoute
                    navController.navigate(
                        AppRoute.PlaybackRoute(
                            streamUrl = streamUrl,
                            contentTitle = contentTitle,
                            contentSynopsis = contentSynopsis,
                            contentId = contentId,
                            type = type
                        )
                    )
                },
                onNavigateToCategory = {navController.navigate(AppRoute.CategoryBrowse(null))},
                isTvMode = isTvMode,
                filterType = "list",
            )
        }

        composable<AppRoute.Details> { backStackEntry ->
            val detailsRoute = backStackEntry.toRoute<AppRoute.Details>()
            DetailsScreen(
                contentId = detailsRoute.id,
                contentType = detailsRoute.type,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayback = { streamUrl, contentTitle, contentSynopsis, contentId, type, playFromBeggining ->
                    // Navigate to PlaybackRoute
                    navController.navigate(
                        AppRoute.PlaybackRoute(
                            streamUrl = streamUrl,
                            contentTitle = contentTitle,
                            contentSynopsis = contentSynopsis,
                            contentId = contentId,
                            type = if (type == ContentType.MOVIE) "movie" else "series",
                            playFromBeggining = playFromBeggining
                        )
                    )
                },
                onEpisodes = { contentId, season ->
                    navController.navigate(AppRoute.Episodes(contentId, season))
                },
                scaffoldPadding = scaffoldPadding,
                isTvMode = isTvMode
            )
        }

        composable<AppRoute.Episodes> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Episodes>()
            // route.contentId and route.season are available via saved state inside EpisodesViewModel
            EpisodesScreenRoute(
                onBack = { navController.popBackStack() },
                onEpisodeSelected = { episode ->
                    val streamUrl = episode.streamUrl?.takeIf { it.isNotBlank() }
                        ?: episode.videoUrl?.takeIf { it.isNotBlank() }

                    if (streamUrl != null) {
                        navController.navigate(
                            AppRoute.PlaybackRoute(
                                streamUrl = streamUrl,
                                contentTitle = episode.title,
                                contentSynopsis = episode.synopsis,
                                contentId = episode.seriesId,
                                type = "series",
                                playFromBeggining = false,
                                season = episode.seasonNumber,
                                episode = episode.episodeNumber
                            )
                        )
                    }
                }
            )
        }
        composable<AppRoute.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTraktAuth = {
                    navController.navigate(AppRoute.TraktAuth)
                },
                isTvMode = isTvMode
            )
        }

        composable<AppRoute.PlaybackRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.PlaybackRoute>()
            PlaybackScreen(
                streamUrl = route.streamUrl,
                contentTitle = route.contentTitle,
                contentSynopsis = route.contentSynopsis,
                contentId = route.contentId,
                contentType = route.type,
                season = route.season,
                episode = route.episode,
                playFromBeggining = route.playFromBeggining,
                isTvMode = isTvMode,
                navController = navController
            )
        }

        composable<AppRoute.Splash> {
            // Get PlayerManager via Hilt (or pass from Activity)
//            val playerManager: PlayerManager = hiltViewModel<SplashViewModel>().playerManager // Or inject differently
//            val splashViewModel: SplashViewModel = hiltViewModel()
//
//            val homeViewModel: HomeViewModel = hiltViewModel()

//            val playerManager: PlayerManager = splashViewModel.playerManager

            SplashScreen(
//                viewModel = splashViewModel,
                homeViewModel = homeViewModel,
//                playerManager = playerManager,
                onNavigateToHome = {
                    navController.navigate(AppRoute.Home) {
                        popUpTo(AppRoute.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<AppRoute.CategoryBrowse> {
            val viewModel: CategoryBrowseViewModel = hiltViewModel()
            CategoryBrowseScreen(
                viewModel = viewModel,
                onNavigateToDetails = { title, synopsis, id, type ->
                    navController.navigate(AppRoute.Details(title,synopsis, id, type))
                }
            )
        }

        // Deep Link for OAuth
        composable<AppRoute.OAuthCallback>(
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "com.stremflix://trakt/callback?code={code}"
                }
            )
        ) { backStackEntry ->
            val callbackRoute = backStackEntry.toRoute<AppRoute.OAuthCallback>()
            OAuthCallbackScreen(
                code = callbackRoute.code,
                onSuccess = {
                    navController.navigate(AppRoute.Home) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                },
                onError = { errorMessage ->
                    // Show error and go back
                    navController.popBackStack()
                    // In real app, show snackbar via ViewModel event
                }
            )
        }
    }
}