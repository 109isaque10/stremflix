// common-ui/src/main/java/com/stremflix/ui/components/ContentCard.kt

package com.stremflix.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.stremflix.ui.R
import com.stremflix.ui.theme.NetflixFocusBorder
import com.stremflix.ui.theme.NetflixSurfaceLight

@Composable
fun ContentCard(
    imageUrl: String?,
    contentDescription: String,
    aspectRatio: Float = 2f / 3f,
    progress: Float? = null,
    isTvMode: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val scale by animateFloatAsState(
        targetValue = if (isFocused && isTvMode) 1.08f else 1f,
        label = "card_scale"
    )

    val sizeModifier = if (isTvMode) {
        Modifier
            .width(if (aspectRatio > 1f) 240.dp else 150.dp)
            .height(if (aspectRatio > 1f) 135.dp else 225.dp)
    } else {
        Modifier
            .width(110.dp) // Original clean mobile sizing constraint
            .aspectRatio(aspectRatio)
    }

    Box(
        modifier = modifier
            .then(sizeModifier)
            .scale(scale)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) NetflixFocusBorder else Color.Transparent,
                shape = RoundedCornerShape(0.dp)
            )
            .background(NetflixSurfaceLight, RoundedCornerShape(6.dp))
            .focusRequester(focusRequester)
            .onFocusChanged { focusState -> isFocused = focusState.isFocused }
            .clickable { onClick() }
            .padding(if (isFocused) 0.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
        ) {
            if (!imageUrl.isNullOrEmpty()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    error = {
                        // If the URL fails to load for ANY network reason, gracefully show the placeholder
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFF333333)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = stringResource(R.string.no_image), color = Color.Gray)
                        }
                    }
                )
            } else {
                // Placeholder when no image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF333333)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        text = stringResource(R.string.no_image),
                        color = Color.Gray
                    )
                }
            }

            // Progress bar for continue watching
            if (progress != null && progress > 0f) {
                Box(modifier = Modifier.matchParentSize().align(Alignment.BottomCenter)) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(Color.Gray.copy(alpha = 0.3f))
                        drawRect(
                            color = NetflixFocusBorder,
                            size = androidx.compose.ui.geometry.Size(
                                width = size.width * progress,
                                height = 4.dp.toPx()
                            )
                        )
                    }
                }
            }
        }
    }
}
