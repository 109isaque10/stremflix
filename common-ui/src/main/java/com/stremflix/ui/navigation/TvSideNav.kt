package com.stremflix.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.stremflix.ui.R
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixRed

data class SideNavItem(
    val route: Any,
    val labelResId: Int,
    val iconResId: Int
)

@Composable
fun TvSideNavigation(
    navController: NavHostController,
    items: List<SideNavItem>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Background dim when expanded
    if (isExpanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { onToggleExpand() }
        )
    }

    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .width(if (isExpanded) 250.dp else 80.dp)
            .background(Color(0xFF141414)),
        containerColor = Color.Transparent,
        contentColor = Color.White,
        header = {
            if (isExpanded) {
                Text(
                    text = "StremFlix",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NetflixRed,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.stremflix_logo), // Placeholder icon
                    contentDescription = "Logo",
                    tint = NetflixRed,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route::class.qualifiedName } == true
            SideNavItemComponent(
                item = item,
                selected = selected,
                isExpanded = isExpanded,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    onToggleExpand() // Close sidebar on selection
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Settings at bottom
        SideNavItemComponent(
            item = SideNavItem(AppRoute.Settings, R.string.nav_settings, R.drawable.ic_settings),
            selected = false,
            isExpanded = isExpanded,
            onClick = {
                navController.navigate(AppRoute.Settings)
                onToggleExpand()
            }
        )
    }
}

@Composable
private fun SideNavItemComponent(
    item: SideNavItem,
    selected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.1f else 1f, label = "scale")

    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Box(
                modifier = Modifier
                    .scale(scale)
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = item.iconResId),
                    contentDescription = null,
                    tint = if (selected) NetflixRed else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        label = if (isExpanded) {
            {
                Text(
                    text = stringResource(id = item.labelResId),
                    color = if (selected) NetflixRed else Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            null
        },
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = NetflixRed,
            selectedTextColor = NetflixRed,
            unselectedIconColor = Color.White,
            unselectedTextColor = Color.Gray,
            indicatorColor = Color.Transparent
        )
    )
}