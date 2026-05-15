package com.stremflix.data.model

data class WatchHistory(
    val seriesId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeId: String,
    val watched: Boolean,
    val watchProgress: Float, // 0.0 to 1.0
    val lastWatchedAt: Long?, // epoch milliseconds
    val title: String,
    val synopsis: String?
) {
    val isCompleted: Boolean get() = watched || watchProgress >= 0.95f
    val isInProgress: Boolean get() = !watched && watchProgress > 0f && watchProgress < 0.95f
}