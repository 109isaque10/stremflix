package com.stremflix.tv.navigation

import androidx.compose.runtime.Composable
import androidx.tv.navigation.rememberTvNavController
import androidx.tv.navigation.TvNavHost
import androidx.tv.navigation.composable

sealed class TvScreen(val route: String) {
    object Home : TvScreen("home")
    object Search : TvScreen("search")
    object MyList : TvScreen("my_list")
    object Settings : TvScreen("settings")
    object Detail : TvScreen("detail/{mediaId}/{mediaType}") {
        fun createRoute(mediaId: String, mediaType: String) = "detail/$mediaId/$mediaType"
    }
    object Player : TvScreen("player/{streamUrl}") {
        fun createRoute(streamUrl: String) = "player/$streamUrl"
    }
}

@Composable
fun TvNavigation() {
    val navController = rememberTvNavController()
    
    TvNavHost(navController = navController, startDestination = TvScreen.Home.route) {
        composable(TvScreen.Home.route) {
            com.stremflix.tv.ui.screens.home.HomeScreen(
                onNavigateToDetail = { mediaId, mediaType ->
                    navController.navigate(TvScreen.Detail.createRoute(mediaId, mediaType))
                },
                onNavigateToPlayer = { streamUrl ->
                    navController.navigate(TvScreen.Player.createRoute(streamUrl))
                }
            )
        }
        composable(TvScreen.Search.route) {
            com.stremflix.tv.ui.screens.search.SearchScreen(
                onNavigateToDetail = { mediaId, mediaType ->
                    navController.navigate(TvScreen.Detail.createRoute(mediaId, mediaType))
                }
            )
        }
        composable(TvScreen.MyList.route) {
            com.stremflix.tv.ui.screens.mylist.MyListScreen(
                onNavigateToDetail = { mediaId, mediaType ->
                    navController.navigate(TvScreen.Detail.createRoute(mediaId, mediaType))
                }
            )
        }
        composable(TvScreen.Settings.route) {
            com.stremflix.tv.ui.screens.settings.SettingsScreen()
        }
        composable(
            route = TvScreen.Detail.route,
            arguments = TvScreen.Detail.arguments
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId") ?: return@composable
            val mediaType = backStackEntry.arguments?.getString("mediaType") ?: return@composable
            com.stremflix.tv.ui.screens.detail.DetailScreen(
                mediaId = mediaId,
                mediaType = mediaType,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { streamUrl ->
                    navController.navigate(TvScreen.Player.createRoute(streamUrl))
                }
            )
        }
        composable(
            route = TvScreen.Player.route,
            arguments = TvScreen.Player.arguments
        ) { backStackEntry ->
            val streamUrl = backStackEntry.arguments?.getString("streamUrl") ?: return@composable
            com.stremflix.tv.ui.screens.player.PlayerScreen(
                streamUrl = streamUrl,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}