package com.stremflix.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.stremflix.data.model.StreamExtra
import com.stremflix.ui.R
import com.stremflix.ui.theme.NetflixTextSecondary

@Composable
fun ExtraBadge(
    streamExtra: Set<StreamExtra>
) {
    var extraResIds = mutableSetOf<Int>()
    streamExtra.forEach { extra ->
        run {
            when (extra) {
                StreamExtra.ATMOS_VISION -> extraResIds.add(R.drawable.ic_dolby_atmos)
//                StreamExtra.DOLBY_VISION -> extraResIds.add(R.drawable.ic_dolby)
                StreamExtra.HDR10 -> extraResIds.add(R.drawable.ic_hdr)
                StreamExtra.HDR10_PLUS -> extraResIds.add(R.drawable.ic_hdr)
                StreamExtra.FOUR_K -> extraResIds.add(R.drawable.ic_badge_fourk)
                StreamExtra.FIVE_POINT_ONE -> extraResIds.add(R.drawable.ic_5dot1)
                StreamExtra.SEVEN_POINT_ONE -> extraResIds.add(R.drawable.ic_7dot1)
                StreamExtra.ATMOS -> extraResIds.add(R.drawable.ic_dolby_atmos)
                StreamExtra.DOLBY_DIGITAL -> extraResIds.add(R.drawable.ic_dolby_digital)
                StreamExtra.DOLBY_DIGITAL_PLUS -> extraResIds.add(R.drawable.ic_dolby_digital_plus)
                StreamExtra.IMAX -> extraResIds.add(R.drawable.ic_imax)
                StreamExtra.IMAX_ENHANCED -> extraResIds.add(R.drawable.ic_imax)
                StreamExtra.DTS -> extraResIds.add(R.drawable.ic_dts)
                StreamExtra.DTS_X -> extraResIds.add(R.drawable.ic_dts)
                StreamExtra.TRUE_HD -> extraResIds.add(R.drawable.ic_dolby_truehd)
                else -> extraResIds.add(R.drawable.ic_question_mark)
            }
        }
        }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        extraResIds.forEach { resId ->
            if(resId == R.drawable.ic_question_mark) return@forEach
            Icon(
                painter = painterResource(id = resId),
                contentDescription = null,
                tint = NetflixTextSecondary,
                modifier = Modifier.height(16.dp)
            )
        }
    }
}