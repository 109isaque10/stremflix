package com.stremflix.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.stremflix.data.model.Episode
import com.stremflix.ui.R
import com.stremflix.ui.theme.NetflixRed
import com.stremflix.ui.theme.NetflixTextPrimary
import com.stremflix.ui.theme.NetflixTextSecondary

@Composable
fun SeasonEpisodeSelector(
    seasons: List<Int>,
    selectedSeason: Int,
    episodes: List<Episode>,
    onSeasonSelected: (Int) -> Unit,
    onEpisodeSelected: (Episode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded = remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(seasons) { seasonNum ->
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (seasonNum == selectedSeason) Color.White else Color(0xFF333333))
                        .clickable { onSeasonSelected(seasonNum) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Season $seasonNum", color = Color.Black)
                }
            }
        }
//        // Season Dropdown
//        ExposedDropdownMenuBox(
//            expanded = expanded.value,
//            onExpandedChange = { expanded.value = !expanded.value }
//        ) {
//            OutlinedTextField(
//                value = "Season $selectedSeason",
//                onValueChange = {},
//                readOnly = true,
//                label = { Text("Select Season", color = NetflixTextSecondary) },
//                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = NetflixRed,
//                    unfocusedBorderColor = Color(0xFF333333)
//                ),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
//            )
//
//            ExposedDropdownMenu(
//                expanded = expanded.value,
//                onDismissRequest = { expanded.value = false }
//            ) {
//                seasons.forEach { seasonNum ->
//                    DropdownMenuItem(
//                        text = { Text("Season $seasonNum", color = NetflixTextPrimary) },
//                        onClick = {
//                            onSeasonSelected(seasonNum)
//                            expanded.value = false
//                        }
//                    )
//                }
//            }
//        }

        Spacer(Modifier.height(16.dp))

        // Episodes List
        Text(
            text = "Episodes",
            style = MaterialTheme.typography.titleMedium,
            color = NetflixTextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (episodes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF1a1a1a), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No episodes found for Season $selectedSeason",
                    color = NetflixTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            episodes.forEach { episode ->
                EpisodeRow(
                    episode = episode,
                    onClick = { onEpisodeSelected(episode) }
                )
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    episode: Episode,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Episode thumbnail or number
        Box(
            modifier = Modifier
                .size(80.dp, 45.dp)
                .background(Color(0xFF333333), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = episode.thumbnailUrl,
                contentDescription = "Episode Thumbnail for E${episode.episodeNumber}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${episode.episodeNumber}. ${episode.title}",
                style = MaterialTheme.typography.bodyMedium,
                color = NetflixTextPrimary,
                fontWeight = FontWeight.Bold
            )

            if (!episode.synopsis.isNullOrEmpty()) {
                Text(
                    text = episode.synopsis!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = NetflixTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Play button or continue indicator
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_play),
            contentDescription = "Play",
            tint = NetflixRed
        )
    }

    Divider(color = Color(0xFF333333))
}
