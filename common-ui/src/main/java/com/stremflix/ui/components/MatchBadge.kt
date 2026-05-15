package com.stremflix.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.stremflix.ui.R
import com.stremflix.ui.theme.NetflixMatchGreen

@Composable
fun MatchBadge(
    score: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = R.string.match_percent, score),
        color = NetflixMatchGreen,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = modifier
    )
}