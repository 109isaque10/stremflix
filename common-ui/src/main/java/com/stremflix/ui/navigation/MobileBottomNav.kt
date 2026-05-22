package com.stremflix.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

data class NavItem(
    val route: Any,
    val labelResId: Int,
    val iconResId: Int
)

@Composable
fun MobileBottomNavigation(
    navController: NavHostController,
    items: List<NavItem>,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = Color(0xFF181818),
        contentColor = Color.White,
        modifier = modifier
    ) {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route::class.qualifiedName } == true
            NavigationBarItem(
                icon = { Icon(ImageVector.vectorResource(id = item.iconResId), contentDescription = null, tint = if(selected) Color.White else Color.Gray) },
                label = { Text(stringResource(id = item.labelResId), color = if(selected) Color.White else Color.Gray, style = MaterialTheme.typography.labelSmall) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemColors(
                    Color.White,
                    Color.White,
                    Color.Transparent,
                    Color.Gray,
                    Color.Gray,
                    Color.Gray,
                    Color.Gray
                )
            )
        }
    }
}