package com.stremflix.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.stremflix.ui.R
import com.stremflix.ui.theme.StremFlixTypography

@Composable
fun AudioSubtitleSelectorMobile(
    audioTracks: List<Track>,
    subtitleTracks: List<Track>,
    onSelect: (trackId: Int?) -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }
    // Use your existing image scrim background; this is a full-screen Dialog
    if(showDialog){
        Dialog(onDismissRequest = {showDialog=false}) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Left column: Audio
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Text("Audio", color = Color.White, style = StremFlixTypography.titleLarge)
                        Spacer(Modifier.height(12.dp))
                        // Build audio track list from player.currentTracks / trackGroups
                        LazyColumn {
                            items(audioTracks) { track ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelect(track.trackFormat.id!!.toInt()) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (track.isSelected) {
                                        Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_check_circle), contentDescription = "Selected", tint = Color.White)
                                        Spacer(Modifier.width(8.dp))
                                    } else {
                                        Spacer(Modifier.width(28.dp))
                                    }
                                    Text(text = track.trackFormat.label!!, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.width(24.dp))

                    // Right column: Subtitles
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Text("Subtitles", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))
                        LazyColumn {
                            items(subtitleTracks) { track ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelect(track.trackFormat.id!!.toInt()) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (track.isSelected) {
                                        Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_check_circle), contentDescription = "Selected", tint = Color.White)
                                        Spacer(Modifier.width(8.dp))
                                    } else {
                                        Spacer(Modifier.width(28.dp))
                                    }
                                    Text(text = track.trackFormat.label!!, color = Color.White)
                                }
                            }
                            // add "Off" option for subtitles
                            item {
                                val isSubtitleOff = subtitleTracks.isEmpty() || subtitleTracks.all { it.isSubtitleOff }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelect(null) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSubtitleOff) {
                                        Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_check_circle), contentDescription = "Selected", tint = Color.White)
                                        Spacer(Modifier.width(8.dp))
                                    } else {
                                        Spacer(Modifier.width(28.dp))
                                    }
                                    Text("Off", color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Bottom-right action bar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
                ) {
                    OutlinedButton(onClick = {showDialog=false}) { Text("Cancel") }
                    Button(onClick = {showDialog=false}) { Text("Apply") }
                }
            }
        }
    }
}

@Composable
fun AudioSubtitleSelectorTvDrawer(
    audioTracks: List<Track>,
    subtitleTracks: List<Track>,
    onSelect: (trackId: Int?) -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }
    // semi-transparent drawer anchored to right
    if(isVisible){
        Box(modifier = Modifier.fillMaxSize()) {
            // Scrim left of drawer
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable { isVisible=false }) {}

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(420.dp)
                    .background(Color(0xDD111111))
                    .align(Alignment.CenterEnd)
                    .padding(16.dp)
            ) {
                // Subtitles section (upper)
                Text("Subtitles", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(subtitleTracks) { track ->
                        val isSelected = track.isSelected
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .then(
                                    if (isSelected) Modifier.border(2.dp, Color.White, RoundedCornerShape(4.dp)) else Modifier
                                )
                                .clickable { onSelect(track.trackFormat.id!!.toInt()) }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Icon(ImageVector.vectorResource(R.drawable.ic_check_circle), contentDescription = null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(track.trackFormat.label!!, color = Color.White)
                            }
                        }
                    }
                    // Off option
                    item {
                        val offSelected = subtitleTracks.isEmpty() || subtitleTracks.all { it.isSubtitleOff }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .then(if (offSelected) Modifier.border(2.dp, Color.White, RoundedCornerShape(4.dp)) else Modifier)
                                .clickable { onSelect(null) }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (offSelected) {
                                    Icon(ImageVector.vectorResource(R.drawable.ic_check_circle), contentDescription = null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text("Off", color = Color.White)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Audio section (lower)
                Text("Audio", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(audioTracks) { track ->
                        val isSelected = track.isSelected
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { onSelect(track.trackFormat.id!!.toInt()) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Icon(ImageVector.vectorResource(R.drawable.ic_check_circle), contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                            } else {
                                Spacer(Modifier.width(28.dp))
                            }
                            Text(track.trackFormat.label!!, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}