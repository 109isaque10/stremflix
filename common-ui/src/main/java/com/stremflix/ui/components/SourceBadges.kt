package com.stremflix.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.stremflix.data.model.StreamSource
import com.stremflix.ui.R
import com.stremflix.ui.theme.NetflixTextSecondary

@Composable
fun SourceBadge(
    streamSource: StreamSource
) {
    val source: Int = when(streamSource) {
        StreamSource.MASTER_QUALITY -> R.drawable.ic_star
        StreamSource.HIGH_QUALITY -> R.drawable.ic_hq
        StreamSource.MEDIUM_QUALITY -> R.drawable.ic_mq
        StreamSource.LOW_QUALITY -> R.drawable.ic_lq
        StreamSource.WORST_QUALITY -> R.drawable.ic_trash
        StreamSource.UNKNOWN -> R.drawable.ic_question_mark
    }

    Icon(
        painter = painterResource(id = source),
        contentDescription = null,
        tint = NetflixTextSecondary,
        modifier = Modifier.height(16.dp)
    )
}