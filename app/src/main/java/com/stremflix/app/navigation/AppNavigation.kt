package com.stremflix.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stremflix.app.ui.screens.home.HomeScreen
import com.stremflix.app.ui.screens.search.SearchScreen
import com.stremflix.app.ui.screens.comingsoon.ComingSoonScreen
import com.stremflix.app.ui.screens.settings.SettingsScreen
import com.stremflix.app.ui.screens.detail.DetailScreen
import com.stremflix.app.ui.screens.player.PlayerScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object ComingSoon : Screen("coming_soon")
    object Settings : Screen("settings")
    object Detail : Screen("detail/{mediaId}/{mediaType}") {
        fun createRoute(mediaId: String, mediaType: String) = "detail/$mediaId/$mediaType"
    }
    object Player : Screen("player/{streamUrl}") {
        fun createRoute(streamUrl: String) = "player/$streamUrl"
    }
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDetail = { mediaId, mediaType ->
                    navController.navigate(Screen.Detail.createRoute(mediaId, mediaType))
                },
                onNavigateToPlayer = { streamUrl ->
                    navController.navigate(Screen.Player.createRoute(streamUrl))
                }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToDetail = { mediaId, mediaType ->
                    navController.navigate(Screen.Detail.createRoute(mediaId, mediaType))
                }
            )
        }
        composable(Screen.ComingSoon.route) {
            ComingSoonScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(
            route = Screen.Detail.route,
            arguments = Screen.Detail.arguments
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId") ?: return@composable
            val mediaType = backStackEntry.arguments?.getString("mediaType") ?: return@composable
            DetailScreen(
                mediaId = mediaId,
                mediaType = mediaType,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { streamUrl ->
                    navController.navigate(Screen.Player.createRoute(streamUrl))
                }
            )
        }
        composable(
            route = Screen.Player.route,
            arguments = Screen.Player.arguments
        ) { backStackEntry ->
            val streamUrl = backStackEntry.arguments?.getString("streamUrl") ?: return@composable
            PlayerScreen(
                streamUrl = streamUrl,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}