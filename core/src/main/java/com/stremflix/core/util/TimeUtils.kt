package com.stremflix.core.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

data class CachedValue<T>(
    val data: T,
    val timestamp: Instant
) {
    fun isExpired(ttl: Duration): Boolean =
        Clock.System.now() - timestamp >= ttl
}

// Simple in-memory cache for demo (replace with proper caching layer in production)
private val memoryCache = mutableMapOf<String, CachedValue<*>>()

@Suppress("UNCHECKED_CAST")
suspend fun <T> cached(
    key: String,
    ttlHours: Long,
    fetcher: suspend () -> T
): T {
    val cached = memoryCache[key] as? CachedValue<T>
    val ttlMillis = ttlHours * 60 * 60 * 1000

    if (cached != null && !cached.isExpired(ttlMillis.hours)) {
        return cached.data
    }

    val fresh = fetcher()
    memoryCache[key] = CachedValue(fresh, Clock.System.now())
    return fresh
}

fun formatDuration(minutes: Int?): String {
    if (minutes == null || minutes <= 0) return ""
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0 -> "${hours}h"
        else -> "${mins}m"
    }
}

fun formatYear(year: Int?): String = year?.toString().orEmpty()

fun formatRating(rating: Float?): String = when {
    rating == null -> ""
    rating >= 10 -> "%.1f".format(rating)
    else -> "%.0f".format(rating)
}

fun formatMatchScore(score: Int): String = "${score}%"