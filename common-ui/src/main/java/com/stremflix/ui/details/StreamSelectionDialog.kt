package com.stremflix.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.stremflix.data.model.Stream
import com.stremflix.ui.components.QualityBadgeRow
import com.stremflix.ui.R
import com.stremflix.ui.theme.NetflixBlack
import com.stremflix.ui.theme.NetflixFocusBorder
import com.stremflix.ui.theme.NetflixRed
import com.stremflix.ui.theme.NetflixSurfaceLight
import com.stremflix.ui.theme.NetflixTextPrimary
import com.stremflix.ui.theme.NetflixTextSecondary
import com.sun.java.swing.plaf.motif.resources.motif_de

@Composable
fun StreamSelectionDialog(
    streams: List<Stream>,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onStreamSelected: (Stream) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = NetflixBlack,
        dragHandle = {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier.width(40.dp).height(4.dp)
                        .background(Color.Gray.copy(0.5f), RoundedCornerShape(2.dp))
                )
            }
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Select Quality",
                style = MaterialTheme.typography.headlineSmall,
                color = NetflixTextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Loading state
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NetflixRed)
                }
            }
            // Empty state
            else if (streams.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_info),
                            null,
                            tint = NetflixTextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No streams available", color = NetflixTextSecondary)
                    }
                }
            }
            // Stream list
            else {
                streams.forEach { stream ->
                    StreamItem(
                        stream = stream,
                        onClick = { onStreamSelected(stream) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StreamItem(
    stream: Stream,
    onClick: () -> Unit
) {
    val mod = Modifier
    Row(
        modifier = mod
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
            .onFocusChanged { mod.border(1.dp, NetflixFocusBorder) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quality Badges (4K, HDR, etc.)
        QualityBadgeRow(
            has4K = stream.quality?.contains("4K", ignoreCase = true) == true || stream.quality?.contains(
                "2160",
                ignoreCase = true
            ) == true,
            has51 = stream.quality?.contains("5.1", ignoreCase = true) == true,
            hasHDR = stream.quality?.contains("HDR", ignoreCase = true) == true || stream.quality?.contains(
                "DV",
                ignoreCase = true
            ) == true,
            modifier = Modifier.width(100.dp)
        )

        // Stream Name/Title
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stream.quality ?: stringResource(id = R.string.stream_default_quality),
                color = NetflixTextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stream.language ?: stringResource(id = R.string.stream_default_lang),
                color = NetflixTextSecondary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Play Icon
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_play),
            contentDescription = "Play",
            tint = NetflixRed
        )
    }
}
