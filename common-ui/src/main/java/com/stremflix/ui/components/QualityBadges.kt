package com.stremflix.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stremflix.ui.R
import com.stremflix.ui.theme.NetflixTextSecondary

@Composable
fun QualityBadgeRow(
    has4K: Boolean,
    has51: Boolean,
    hasHDR: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (has4K) QualityBadge(textRes = R.string.badge_4k)
        if (has51) QualityBadge(textRes = R.string.badge_5_1)
        if (hasHDR) QualityBadge(textRes = R.string.badge_hdr)
    }
}

@Composable
fun QualityBadge(
    textRes: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = textRes),
        color = NetflixTextSecondary,
        fontSize = 12.sp,
        modifier = modifier
            .border(1.dp, NetflixTextSecondary.copy(alpha = 0.5f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}