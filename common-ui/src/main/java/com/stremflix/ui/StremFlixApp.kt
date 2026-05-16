package com.stremflix.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
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
                    SideNavItem(AppRoute.Search, R.string.nav_search, R.drawable.ic_search),
                    SideNavItem(AppRoute.Home, R.string.nav_home, R.drawable.ic_home),
                    SideNavItem(AppRoute.TVShows, R.string.nav_tv_shows, R.drawable.ic_tv),
                    SideNavItem(AppRoute.Movies, R.string.nav_movies, R.drawable.ic_movies),
                    SideNavItem(AppRoute.MyList, R.string.nav_my_list, R.drawable.ic_list)
                )
            } else {
                listOf(
                    BottomNavItem(AppRoute.Search, R.string.nav_search, R.drawable.ic_search),
                    BottomNavItem(AppRoute.Home, R.string.nav_home, R.drawable.ic_home),
                    BottomNavItem(AppRoute.TVShows, R.string.nav_tv_shows, R.drawable.ic_tv),
                    BottomNavItem(AppRoute.Movies, R.string.nav_movies, R.drawable.ic_movies),
                    BottomNavItem(AppRoute.MyList, R.string.nav_my_list, R.drawable.ic_list)
                )
            }

            Scaffold(
                bottomBar = {
                    if (!isTvMode && !isPlayerScreen) {
                        MobileBottomNavigation(
                            navController = navController,
                            items = navItems as List<BottomNavItem>
                        )
                    }
                },
                containerColor = NetflixBlack,
                contentColor = Color.White
            ) { paddingValues ->
                val boxModifier = if (isPlayerScreen) Modifier.fillMaxSize() else Modifier.padding(paddingValues)
                Box(modifier = boxModifier) {
                    StremFlixNavGraph(
                        navController = navController,
                        isTvMode = isTvMode
                    )

                    // TV Sidebar Overlay
                    if (isTvMode) {
                        var isNavVisible by remember { mutableStateOf(false) }

                        // Simple toggle logic for demo; in real app, handle D-pad left edge
                        TvSideNavigation(
                            navController = navController,
                            items = navItems as List<SideNavItem>,
                            isExpanded = isNavVisible,
                            onToggleExpand = { isNavVisible = !isNavVisible },
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                    }
                }
            }
        }
    }
}