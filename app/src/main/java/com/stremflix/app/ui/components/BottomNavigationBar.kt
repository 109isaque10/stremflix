package com.stremflix.app.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar {
        tabs.forEachIndexed { index, label ->
            NavigationBarItem(
                icon = {
                    // Icons would go here
                    Text(label.take(1))
                },
                label = { Text(label) },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) }
            )
        }
    }
}