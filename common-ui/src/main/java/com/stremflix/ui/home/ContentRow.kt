package com.stremflix.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stremflix.data.model.ContentItem
import com.stremflix.ui.components.ContentCard
import com.stremflix.ui.theme.NetflixFocusBorder
import com.stremflix.ui.theme.NetflixTextPrimary

@Composable
fun ContentRow(
    title: String,
    items: List<ContentItem>,
    isLarge: Boolean = false,
    onItemSelected: (ContentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = NetflixTextPrimary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        val mod = Modifier
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = mod.onFocusChanged { mod.border(1.dp, NetflixFocusBorder) }
        ) {
            items(items, key = {it.id}) { item ->
                ContentCard(
                    imageUrl = item.posterUrl,
                    contentDescription = item.title,
                    aspectRatio = if (isLarge) 16f/9f else 2f/3f,
                    progress = if (item.watchProgress > 0f) item.watchProgress else null,
                    onClick = { onItemSelected(item) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}