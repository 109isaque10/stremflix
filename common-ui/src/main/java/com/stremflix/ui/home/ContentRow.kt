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
import com.stremflix.ui.theme.StremFlixTypography

@Composable
fun ContentRow(
    title: String,
    items: List<ContentItem>,
    isLarge: Boolean = false,
    isTvMode: Boolean = false,
    onItemSelected: (ContentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().wrapContentHeight()) {
        Text(
            text = title,
            style = if(isTvMode) StremFlixTypography.titleLarge else StremFlixTypography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NetflixTextPrimary,
            modifier = Modifier.padding(horizontal = if (isTvMode) 24.dp else 16.dp,
                vertical = if (isTvMode) 6.dp else 4.dp)
        )
        val mod = Modifier
        LazyRow(
            contentPadding = PaddingValues(horizontal = if (isTvMode) 24.dp else 16.dp,
                vertical = if (isTvMode) 8.dp else 4.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isTvMode) 12.dp else 8.dp),
            modifier = mod.fillMaxWidth().wrapContentHeight().onFocusChanged { mod.border(1.dp, NetflixFocusBorder) }
        ) {
            items(items, key = {it.id}) { item ->
                ContentCard(
                    imageUrl = item.posterUrl,
                    contentDescription = item.title,
                    aspectRatio = if (isLarge) 16f/9f else 2f/3f,
                    isTvMode = isTvMode,
                    progress = if (item.watchProgress > 0f) item.watchProgress else null,
                    onClick = { onItemSelected(item) }
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isTvMode) 12.dp else 8.dp))
    }
}