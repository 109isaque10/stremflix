// common-ui/src/main/java/com/stremflix/ui/navigation/TvSideNav.kt
package com.stremflix.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

@Composable
fun TvSideNav(
    navItems: List<NavItem>, // Reusing your existing nav items
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isNavFocused by remember { mutableStateOf(false) }

    val navWidth by animateDpAsState(
        targetValue = if (isNavFocused) 220.dp else 64.dp,
        animationSpec = tween(durationMillis = 300),
        label = "nav_width"
    )

    val bgAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isNavFocused) 0.9f else 0.0f,
        label = "bg_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(navWidth)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color.Black.copy(alpha = bgAlpha), Color.Transparent)
                )
            )
            // 'hasFocus' is true if ANY child item inside this Column has the D-Pad focus
            .onFocusChanged { isNavFocused = it.hasFocus }
            .padding(vertical = 32.dp),
        verticalArrangement = Arrangement.Center, // Centers items vertically like Netflix
        horizontalAlignment = Alignment.Start
    ) {
        navItems.forEach { item ->
            val isSelected = currentRoute == item.route

            TvNavItem(
                item = item,
                isSelected = isSelected,
                isNavExpanded = isNavFocused,
                onClick = { onNavigate(item.route as String) }
            )
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
    // Track focus for this specific item (to change its color when hovering over it)
    var isItemFocused by remember { mutableStateOf(false) }

    // Netflix styling: White if hovering or selected, Gray if inactive
    val contentColor = when {
        isItemFocused -> Color.White
        isSelected -> Color.White
        else -> Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 20.dp)
            .focusable() // ✅ CRITICAL: Makes this row selectable by the TV D-Pad
            .onFocusChanged { isItemFocused = it.isFocused }
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(item.iconResId),
            contentDescription = stringResource(id = item.labelResId),
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )

        AnimatedVisibility(
            visible = isNavExpanded,
            enter = fadeIn(tween(300, delayMillis = 100)), // Slight delay looks premium
            exit = fadeOut(tween(150))
        ) {
            Text(
                text = stringResource(id = item.labelResId),
                color = contentColor,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
}