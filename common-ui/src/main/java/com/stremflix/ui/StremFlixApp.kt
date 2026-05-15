package com.stremflix.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.stremflix.ui.navigation.*
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.StremFlixTheme
import androidx.compose.ui.Alignment

@Composable
fun StremFlixApp(
    isTvMode: Boolean = false
) {
    StremFlixTheme {
        Surface(color = NetflixBlack) {
            val navController = rememberNavController()

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
                    if (!isTvMode) {
                        MobileBottomNavigation(
                            navController = navController,
                            items = navItems as List<BottomNavItem>
                        )
                    }
                },
                containerColor = NetflixBlack,
                contentColor = Color.White
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
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