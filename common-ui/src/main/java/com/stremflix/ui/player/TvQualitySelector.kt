// common-ui/src/main/java/com/stremflix/ui/player/TvQualitySelector.kt

package com.stremflix.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.stremflix.data.model.Stream
import com.stremflix.data.model.StreamQuality
import com.stremflix.ui.R
import com.stremflix.ui.components.ExtraBadge
import com.stremflix.ui.components.SourceBadge
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixRed
import com.stremflix.ui.theme.NetflixTextSecondary

@Composable
fun TvQualitySelector(
    streams: List<Stream>,
    isLoading: Boolean = false,
    onStreamSelected: (Stream) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(480.dp)
                .background(NetflixBlack, RoundedCornerShape(8.dp))
                .padding(24.dp)
        ) {
            Text(
                text = "Select Quality",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NetflixRed)
                }
            } else if (streams.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Text("No streams available", color = NetflixTextSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 360.dp)
                ) {
                    items(streams) { stream ->
                        TvStreamItemRow(
                            stream = stream,
                            onClick = { onStreamSelected(stream) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvStreamItemRow(
    stream: Stream,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val streamQuality = when(stream.quality) {
        StreamQuality.P_2160 -> "4K"
        StreamQuality.P_1080 -> "FHD"
        StreamQuality.P_720 -> "HD"
        StreamQuality.P_480 -> "SD"
        else -> stringResource(id = R.string.stream_default_quality)
    }

    Surface(
        onClick = onClick,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFF222222),
            focusedContainerColor = Color.White,
            contentColor = Color.LightGray,
            focusedContentColor = Color.Black
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp)),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.5.dp else 0.dp,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (stream.description == "No Streams Found") {
                Text(
                    text = "No streams found",
                    color = if (isFocused) Color.Black else NetflixTextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                return@Surface
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = streamQuality,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFocused) Color.Black else Color.White
                    )
                    stream.source?.let { SourceBadge(streamSource = it) }
                    stream.extra?.let { extras -> if (extras.isNotEmpty()) ExtraBadge(streamExtra = extras) }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stream.language ?: stringResource(id = R.string.stream_default_lang),
                    fontSize = 12.sp,
                    color = if (isFocused) Color.DarkGray else NetflixTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_play),
                contentDescription = "Play",
                tint = NetflixRed,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}