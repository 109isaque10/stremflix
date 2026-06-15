//// common-ui/src/main/java/com/stremflix/ui/navigation/TvSideNav.kt
//package com.stremflix.ui.navigation
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.animateDpAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.focusable
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.focus.onFocusChanged
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.res.vectorResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//@Composable
//fun TvSideNav(
//    navItems: List<NavItem>, // Reusing your existing nav items
//    onNavigate: (Any) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    var selectedTab by remember { mutableStateOf<AppRoute>(AppRoute.Home) }
//    var isNavFocused by remember { mutableStateOf(false) }
//
//    val navWidth by animateDpAsState(
//        targetValue = if (isNavFocused) 220.dp else 64.dp,
//        animationSpec = tween(durationMillis = 300),
//        label = "nav_width"
//    )
//
//    val bgAlpha by androidx.compose.animation.core.animateFloatAsState(
//        targetValue = if (isNavFocused) 0.5f else 0.0f,
//        label = "bg_alpha"
//    )
//
//    Column(
//        modifier = modifier
//            .fillMaxHeight()
//            .width(navWidth)
//            .background(
//                Brush.horizontalGradient(
//                    colors = listOf(Color.Black.copy(alpha = bgAlpha), Color.Transparent)
//                )
//            )
//            // 'hasFocus' is true if ANY child item inside this Column has the D-Pad focus
//            .onFocusChanged { isNavFocused = it.hasFocus }
//            .padding(vertical = 32.dp),
//        verticalArrangement = Arrangement.Center, // Centers items vertically like Netflix
//        horizontalAlignment = Alignment.Start
//    ) {
//        navItems.forEach { item ->
//            val selected = selectedTab == item.route
//
//            TvNavItem(
//                item = item,
//                isSelected = selected,
//                isNavExpanded = isNavFocused,
//                onClick = {
//                    selectedTab = item.route as AppRoute
//                    onNavigate(item.route)
//                }
//            )
//        }
//    }
//}
//
//@Composable
//private fun TvNavItem(
//    item: NavItem,
//    isSelected: Boolean,
//    isNavExpanded: Boolean,
//    onClick: () -> Unit
//) {
//    // Track focus for this specific item (to change its color when hovering over it)
//    var isItemFocused by remember { mutableStateOf(false) }
//
//    // Netflix styling: White if hovering or selected, Gray if inactive
//    val contentColor = when {
//        isItemFocused -> Color.White
//        isSelected -> Color.White
//        else -> Color.Gray
//    }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 16.dp, horizontal = 20.dp)
//            .focusable()
//            .onFocusChanged { isItemFocused = it.isFocused }
//            .clickable(onClick = onClick)
//            .background(
//                if (isSelected || isItemFocused)
//                    Color.White.copy(alpha = 0.12f)
//                else
//                    Color.Transparent
//            ),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            imageVector = ImageVector.vectorResource(item.iconResId),
//            contentDescription = null,
//            tint = contentColor
//        )
//        AnimatedVisibility(visible = isNavExpanded) {
//            Spacer(Modifier.width(16.dp))
//            Text(
//                text = stringResource(id = item.labelResId),
//                color = contentColor,
//                fontSize = 20.sp,
//                fontWeight = FontWeight.SemiBold
//            )
//        }
//    }
//}
package com.stremflix.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import com.stremflix.ui.theme.NetflixRed

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TvSideNav(
    navItems: List<NavItem>,
    onNavigate: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    // Treat the initial route safely. Cast/fallback if necessary depending on your AppRoute definition
    var selectedTab by remember { mutableStateOf<Any>(AppRoute.Home) }
    var isNavFocused by remember { mutableStateOf(false) }

    val navWidth by animateDpAsState(
        targetValue = if (isNavFocused) 240.dp else 72.dp, // Slightly wider for proper padding balance on TV
        animationSpec = tween(durationMillis = 250),
        label = "nav_width"
    )

    val bgAlpha by animateFloatAsState(
        targetValue = if (isNavFocused) 0.85f else 0.0f,
        animationSpec = tween(durationMillis = 250),
        label = "bg_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(navWidth)
            .focusGroup()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = bgAlpha),
                        Color.Black.copy(alpha = bgAlpha * 0.3f),
                        Color.Transparent
                    )
                )
            )
            // Checks if D-pad focus entered ANY row inside this container
            .onFocusChanged { isNavFocused = it.hasFocus }
            .padding(vertical = 32.dp, horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp), // Cleaner spacing matrix for TV
            horizontalAlignment = Alignment.Start
        ) {
            navItems.forEach { item ->
                val isSelected = selectedTab == item.route

                TvNavItem(
                    item = item,
                    isSelected = isSelected,
                    isNavExpanded = isNavFocused,
                    onClick = {
                        selectedTab = item.route
                        onNavigate(item.route)
                    }
                )
            }
        }
    }
}

@Composable
private fun TvNavItem(
    item: NavItem,
    isSelected: Boolean,
    isNavExpanded: Boolean,
    onClick: () -> Unit
) {
    var isItemFocused by remember { mutableStateOf(false) }

    val contentColor = when {
        isItemFocused -> Color.White
        isSelected -> Color.White
        else -> Color.Gray
    }

    Surface(
        onClick = onClick,
        colors = ClickableSurfaceDefaults.colors(
//            containerColor = when {
//                isSelected && isNavExpanded -> Color.White.copy(alpha = 0.1f)
//                isSelected -> Color.White.copy(alpha = 0.05f) // Dimmer indicator when focus is on the main content
//                else -> Color.Transparent
//            },
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent,
            contentColor = contentColor,
            focusedContentColor = Color.White
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(0.dp)),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isItemFocused = it.isFocused } // Updates focus on remote movement
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.BottomCenter) {
                    Icon(
                        imageVector = ImageVector.vectorResource(item.iconResId),
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(26.dp)
                    )
                    // ✅ Netflix Red Bar indicator placed directly underneath the icon when selected
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .padding(top = 32.dp)
                                .width(26.dp)
                                .height(3.dp)
                                .background(NetflixRed) // Netflix Red
                        )
                    }
                }

                AnimatedVisibility(visible = isNavExpanded) {
                    Row {
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = stringResource(id = item.labelResId),
                            color = contentColor,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected || isItemFocused) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
