package com.stremflix.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.stremflix.ui.navigation.*
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.StremFlixTheme

@Composable
fun StremFlixApp(
    isTvMode: Boolean = false
) {
    StremFlixTheme {
        Surface(color = NetflixBlack) {
            val navController = rememberNavController()

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val isPlayerScreen = currentRoute?.contains("PlaybackRoute") == true

            // Determine navigation items based on mode
            val navItems = if (isTvMode) {
                listOf(
                    NavItem(AppRoute.Search, R.string.nav_search, R.drawable.ic_search),
                    NavItem(AppRoute.Home, R.string.nav_home, R.drawable.ic_home),
                    NavItem(AppRoute.TVShows, R.string.nav_tv_shows, R.drawable.ic_tv),
                    NavItem(AppRoute.Movies, R.string.nav_movies, R.drawable.ic_movies),
                    NavItem(AppRoute.CategoryBrowse, R.string.nav_categories, R.drawable.ic_trakt),
                    NavItem(AppRoute.MyList, R.string.nav_my_list, R.drawable.ic_list)
                )
            } else {
                listOf(
                    NavItem(AppRoute.Search, R.string.nav_search, R.drawable.ic_search),
                    NavItem(AppRoute.Home, R.string.nav_home, R.drawable.ic_home),
                    NavItem(AppRoute.TVShows, R.string.nav_tv_shows, R.drawable.ic_tv),
                    NavItem(AppRoute.Movies, R.string.nav_movies, R.drawable.ic_movies),
                    NavItem(AppRoute.MyList, R.string.nav_my_list, R.drawable.ic_list)
                )
            }

            Scaffold(
                bottomBar = {
                    if (!isTvMode && !isPlayerScreen) {
                        MobileBottomNavigation(
                            navController = navController,
                            items = navItems as List<NavItem>
                        )
                    }
                },
                containerColor = NetflixBlack,
                contentColor = Color.White
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {
                    val boxModifier = if (isPlayerScreen) Modifier.fillMaxSize() else Modifier.padding(paddingValues)
                    Box(modifier = boxModifier) {
                        StremFlixNavGraph(
                            navController = navController,
                            isTvMode = isTvMode
                        )
                    }
                    // TV Sidebar Overlay
                    if (isTvMode && !isPlayerScreen) {
                        TvSideNav(
                            navItems = navItems,
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            // Position it locked to the left edge of the screen
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                    }
                }
            }
        }
    }
}