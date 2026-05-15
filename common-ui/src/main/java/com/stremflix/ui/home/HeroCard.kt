package com.stremflix.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.stremflix.data.model.ContentItem
import com.stremflix.ui.R
import com.stremflix.ui.components.MatchBadge
import com.stremflix.ui.components.QualityBadgeRow
import com.stremflix.ui.components.VerticalFadeOverlay
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixMatchGreen
import com.stremflix.ui.theme.NetflixRed
import com.stremflix.ui.theme.NetflixTextPrimary
import com.stremflix.ui.theme.NetflixTextSecondary

@Composable
fun HeroCard(
    item: ContentItem,
    onPlayClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVideoPlaying by remember { mutableStateOf(false) }

    // Auto-play logic: In a real app, this would trigger on focus or after a delay.
    // For this component, we assume it's the main hero and plays automatically.
    LaunchedEffect(Unit) {
        // Simulate 5s idle timeout logic or immediate play
        kotlinx.coroutines.delay(1000)
        isVideoPlaying = true
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(500.dp)
            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
    ) {
        // Background Image (Fallback if video fails or while loading)
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.backdropUrl)
                .crossfade(true)
                .build(),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Vertical Fade Overlay (Strictly between text and backdrop)
        VerticalFadeOverlay(
            modifier = Modifier.fillMaxSize(),
            topAlpha = 0f,
            bottomAlpha = 1f // Fully opaque at bottom for text readability
        ) {
            Box(modifier = Modifier.fillMaxSize())
        }

        // Metadata Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, bottom = 24.dp, end = 24.dp)
                .align(Alignment.BottomStart),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Title Logo or Text
            Text(
                text = item.title,
                style = MaterialTheme.typography.displayLarge,
                color = NetflixTextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Metadata Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                item.matchScore?.let { MatchBadge(score = it) }

                Text(
                    text = item.year?.toString() ?: "",
                    color = NetflixTextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )

                Text(
                    text = item.contentRating ?: "", // Dynamic certification (e.g., "TV-14", "R")
                    color = NetflixTextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .background(Color(0xFF333333), RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )

                if (item.runtime != null && item.runtime!! > 0) {
                    Text(
                        text = "${item.runtime}m",
                        color = NetflixTextSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Synopsis
            Text(
                text = item.synopsis ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = NetflixTextSecondary,
                maxLines = 3,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_play),
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.detail_play),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onMoreInfoClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF333333),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.detail_more_info),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}