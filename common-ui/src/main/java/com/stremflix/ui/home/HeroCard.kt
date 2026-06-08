package com.stremflix.ui.home

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.stremflix.core.domain.model.ContentType
import com.stremflix.data.model.ContentItem
import com.stremflix.ui.R
import com.stremflix.ui.components.MatchBadge
import com.stremflix.ui.components.VerticalFadeOverlay
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixRed
import com.stremflix.ui.theme.NetflixTextPrimary
import com.stremflix.ui.theme.NetflixTextSecondary

//@Composable
//fun HeroCard(
//    item: ContentItem,
//    onPlayClick: () -> Unit,
//    onMoreInfoClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    var isVideoPlaying by remember { mutableStateOf(false) }
//
//    // Auto-play logic: In a real app, this would trigger on focus or after a delay.
//    // For this component, we assume it's the main hero and plays automatically.
//    LaunchedEffect(Unit) {
//        // Simulate 5s idle timeout logic or immediate play
//        kotlinx.coroutines.delay(5000)
//        isVideoPlaying = true
//    }
//
//    Box(
//        modifier = modifier
//            .fillMaxWidth()
//            .height(500.dp)
////            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)) Netflix is squared
//            .background(NetflixBlack)
//    ) {
//        val imageUrl = item.backdropUrl ?: item.posterUrl
//        // Background Image (Fallback if video fails or while loading)
//        if (!imageUrl.isNullOrEmpty()) {
//          SubcomposeAsyncImage(
//              model = ImageRequest.Builder(LocalContext.current)
//                  .data(imageUrl)
//                  .crossfade(true)
//                  .build(),
//              contentDescription = item.title,
//              contentScale = ContentScale.Crop,
//              modifier = Modifier.fillMaxSize(),
//              loading = {
//                  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                      CircularProgressIndicator(color = NetflixRed)
//                  }
//              },
//              error = {
//                  Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
//              }
//          )
//      }
//
//        // Vertical Fade Overlay (Strictly between text and backdrop)
//        VerticalFadeOverlay(
//            modifier = Modifier.fillMaxSize(),
//            topAlpha = 0f,
//            bottomAlpha = 1f // Fully opaque at bottom for text readability
//        ) {
//            Box(modifier = Modifier.fillMaxSize())
//        }
//
//        // Metadata Content
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(start = 24.dp, bottom = 24.dp, end = 24.dp)
//                .align(Alignment.BottomStart),
//            verticalArrangement = Arrangement.Bottom
//        ) {
//            // Title Logo or Text
////            Text(
////                text = item.title,
////                style = MaterialTheme.typography.displayLarge,
////                color = NetflixTextPrimary,
////                modifier = Modifier.padding(bottom = 8.dp)
////            )
//            SubcomposeAsyncImage(
//                model = ImageRequest.Builder(LocalContext.current)
//                    .data(item.titleLogoUrl ?: item.title) // Fallback to title text if logo is unavailable
//                    .crossfade(true)
//                    .build(),
//                contentDescription = item.title,
//                contentScale = ContentScale.Fit,
//                modifier = Modifier
//                    .height(80.dp)
//                    .padding(bottom = 8.dp),
//                loading = {
//                    Text(
//                        text = item.title,
//                        style = MaterialTheme.typography.displayLarge,
//                        color = NetflixTextPrimary
//                    )
//                },
//                error = {
//                    Text(
//                        text = item.title,
//                        style = MaterialTheme.typography.displayLarge,
//                        color = NetflixTextPrimary
//                    )
//                }
//            )
//
//            // Metadata Row
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                modifier = Modifier.padding(bottom = 16.dp)
//            ) {
//                item.matchScore?.let { MatchBadge(score = it) }
//
//                Text(
//                    text = item.year?.toString() ?: "",
//                    color = NetflixTextSecondary,
//                    style = MaterialTheme.typography.labelMedium
//                )
//
//                if(item.type == ContentType.SERIES)
//                    Text(
//                        text = item.numberOfSeasons?.let { "$it ${R.string.detail_seasons}" } ?: "",
//                        color = NetflixTextSecondary,
//                        style = MaterialTheme.typography.labelMedium
//                    )
//
//                Text(
//                    text = item.contentRating ?: "", // Dynamic certification (e.g., "TV-14", "R")
//                    color = NetflixTextSecondary,
//                    style = MaterialTheme.typography.labelMedium,
//                    modifier = Modifier
//                        .background(Color(0xFF333333), RoundedCornerShape(2.dp))
//                        .padding(horizontal = 4.dp, vertical = 2.dp)
//                )
//
//                if (item.runtime != null && item.runtime!! > 0) {
//                    Text(
//                        text = "${item.runtime}m",
//                        color = NetflixTextSecondary,
//                        style = MaterialTheme.typography.labelMedium
//                    )
//                }
//            }
//
//            // Synopsis
//            Text(
//                text = item.synopsis ?: "",
//                style = MaterialTheme.typography.bodyMedium,
//                color = NetflixTextSecondary,
//                maxLines = 3,
//                modifier = Modifier.padding(bottom = 24.dp)
//            )
//
//            // Action Buttons
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Button(
//                    onClick = onPlayClick,
//                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
//                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
//                    shape = RoundedCornerShape(4.dp)
//                ) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            imageVector = ImageVector.vectorResource(R.drawable.ic_play),
//                            contentDescription = null,
//                            tint = Color.Black,
//                            modifier = Modifier.size(24.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = stringResource(id = R.string.detail_play),
//                            color = Color.Black,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
//
//                OutlinedButton(
//                    onClick = onMoreInfoClick,
//                    colors = ButtonDefaults.outlinedButtonColors(
//                        containerColor = Color(0xFF333333),
//                        contentColor = Color.White
//                    ),
//                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
//                    shape = RoundedCornerShape(4.dp)
//                ) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text(
//                            text = stringResource(id = R.string.detail_more_info),
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

// --- HeroCardMobile + HeroCardTv + HeroCard wrapper (paste into HeroCard.kt) ---

@Composable
fun HeroCardMobile(
    item: ContentItem,
    onPlayClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
            .background(NetflixBlack)
    ) {
        // Fallback image (backdrop or poster)
        val imageUrl = item.backdropUrl ?: item.posterUrl
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NetflixRed)
                }
            },
            error = {
                Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
            }
        )

        // Horizontal scrim and vertical bottom fade for text readability
        Box(modifier = Modifier.matchParentSize()) {
            // horizontal scrim left -> transparent
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawWithContent {
                        drawContent()
                        val w = size.width
                        val brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.92f),
                                Color.Black.copy(alpha = 0.45f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = w * 0.65f
                        )
                        drawRect(brush = brush, size = size)
                    }
            )
            // vertical bottom fade for safer text overlay
            VerticalFadeOverlay(modifier = Modifier.matchParentSize(), topAlpha = 0f, bottomAlpha = 1f) {}
        }

        // Metadata overlaid at bottom-left (image-only style)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 20.dp, end = 24.dp)
                .fillMaxSize(), // leave some right breathing room for text readability
            verticalArrangement = Arrangement.Bottom
        ) {
            // Title logo (if present) or plain title fallback
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(item.titleLogoUrl ?: item.title)
                    .crossfade(true)
                    .build(),
                contentDescription = item.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(64.dp)
                    .padding(bottom = 8.dp),
                loading = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.displaySmall,
                        color = NetflixTextPrimary
                    )
                },
                error = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.displaySmall,
                        color = NetflixTextPrimary
                    )
                }
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item.matchScore?.let { MatchBadge(score = it) }
                Text(text = item.year?.toString() ?: "", color = NetflixTextSecondary, style = MaterialTheme.typography.labelMedium)
                if (item.type == ContentType.SERIES)
                    Text(text = item.numberOfSeasons?.let { "$it ${R.string.detail_seasons}" } ?: "", color = NetflixTextSecondary, style = MaterialTheme.typography.labelMedium)
                Text(text = item.contentRating ?: "", color = NetflixTextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.background(Color(0xFF333333), RoundedCornerShape(2.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
                if (item.runtime != null && item.runtime!! > 0) Text(text = "${item.runtime}m", color = NetflixTextSecondary, style = MaterialTheme.typography.labelMedium)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = item.synopsis ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = NetflixTextSecondary,
                maxLines = 3,
                modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onPlayClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), shape = RoundedCornerShape(4.dp)) {
                    Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_play), contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.detail_play), color = Color.Black, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(onClick = onMoreInfoClick, colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF333333), contentColor = Color.White), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), shape = RoundedCornerShape(4.dp)) {
                    Text(text = stringResource(id = R.string.detail_more_info), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HeroCardTv(
    item: ContentItem,
    onPlayClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
    autoPlayPreview: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showPosterOverlay by remember { mutableStateOf(true) }
    var trailerLoadFailed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(520.dp)
            .background(NetflixBlack)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left: metadata area (solid dark region)
            Box(
                modifier = Modifier
                    .width(520.dp)
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.fillMaxHeight().padding(top = 24.dp), verticalArrangement = Arrangement.Bottom) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context).data(item.titleLogoUrl ?: item.title).crossfade(true).build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.height(80.dp).padding(bottom = 8.dp),
                        loading = { Text(text = item.title, style = MaterialTheme.typography.displaySmall, color = NetflixTextPrimary) },
                        error = { Text(text = item.title, style = MaterialTheme.typography.displaySmall, color = NetflixTextPrimary) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                        item.matchScore?.let { MatchBadge(score = it) }

                        Text(text = item.year?.toString() ?: "", color = NetflixTextSecondary, style = MaterialTheme.typography.labelMedium)

                        if (item.type == ContentType.SERIES)
                            Text(text = item.numberOfSeasons?.let { "$it ${R.string.detail_seasons}" } ?: "", color = NetflixTextSecondary, style = MaterialTheme.typography.labelMedium)

                        Text(text = item.contentRating ?: "", color = NetflixTextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.background(Color(0xFF333333), RoundedCornerShape(2.dp)).padding(horizontal = 4.dp, vertical = 2.dp))

                        if (item.runtime != null && item.runtime!! > 0) Text(text = "${item.runtime}m", color = NetflixTextSecondary, style = MaterialTheme.typography.labelMedium)
                    }

                    Text(text = item.synopsis ?: "", style = MaterialTheme.typography.bodyMedium, color = NetflixTextSecondary, maxLines = 3, modifier = Modifier.padding(bottom = 24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onPlayClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp), shape = RoundedCornerShape(4.dp)) {
                            Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_play), contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(id = R.string.detail_play), color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(onClick = onMoreInfoClick, colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF333333), contentColor = Color.White), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp), shape = RoundedCornerShape(4.dp)) {
                            Text(text = stringResource(id = R.string.detail_more_info), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Right: trailer / preview area (fills remaining)
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                // If we have a YouTube id and it hasn't failed, use AndroidView to host YouTubePlayerView
                if (!item.trailerId.isNullOrBlank() && !trailerLoadFailed) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            val youTubePlayerView = YouTubePlayerView(ctx).apply {
                                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            }

                            lifecycleOwner.lifecycle.addObserver(youTubePlayerView)

                            youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                override fun onReady(youTubePlayer: YouTubePlayer) {
                                    try {
                                        if (autoPlayPreview && !showPosterOverlay) youTubePlayer.loadVideo(item.trailerId!!, 0f)
                                        else youTubePlayer.cueVideo(item.trailerId!!, 0f)
                                    } catch (t: Throwable) {
                                        // fallback
                                        trailerLoadFailed = true
                                    }
                                }

                                override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                                    if (state == PlayerConstants.PlayerState.PLAYING) {
                                        android.os.Handler(android.os.Looper.getMainLooper()).post { showPosterOverlay = false }
                                    } else if (state == PlayerConstants.PlayerState.ENDED) {
                                        // optional behavior
                                    }
                                }

                                override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        trailerLoadFailed = true
                                        showPosterOverlay = true
                                    }
                                }
                            })
                            youTubePlayerView
                        },
                        update = { /* no-op for now */ }
                    )

                    // Poster overlay while trailer isn't yet playing
                    val posterUrl = item.backdropUrl ?: item.posterUrl
                    if (showPosterOverlay && !(posterUrl.isNullOrBlank())) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context).data(posterUrl).crossfade(true).build(),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NetflixRed) } },
                            error = { Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) }
                        )
                    }
                } else {
                    // fallback to backdrop image if no trailer
                    val posterUrl = item.backdropUrl ?: item.posterUrl
                    if (!posterUrl.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context).data(posterUrl).crossfade(true).build(),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NetflixRed) } },
                            error = { Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
                    }
                }
            }
        } // end Row

        // Horizontal scrim overlay (keep above content so left metadata remains readable)
        Box(modifier = Modifier.matchParentSize().drawWithContent {
            drawContent()
            val w = size.width
            val brush = Brush.horizontalGradient(
                colors = listOf(Color.Black.copy(alpha = 0.94f), Color.Black.copy(alpha = 0.6f), Color.Transparent),
                startX = 0f,
                endX = w * 0.65f
            )
            drawRect(brush = brush, size = size)
        })
    }
}

/**
 * Optional wrapper: choose by boolean. Prefer calling the mobile or tv composables directly
 * if you already detect TV vs mobile in your code.
 */
@Composable
fun HeroCard(
    item: ContentItem,
    onPlayClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
    isTv: Boolean,
    modifier: Modifier = Modifier
) {
    if (isTv) {
        HeroCardTv(item = item, onPlayClick = onPlayClick, onMoreInfoClick = onMoreInfoClick, modifier = modifier)
    } else {
        HeroCardMobile(item = item, onPlayClick = onPlayClick, onMoreInfoClick = onMoreInfoClick, modifier = modifier)
    }
}