package com.stremflix.data.model

data class Stream(
    val description: String?,
    val url: String,
    val quality: StreamQuality?,
    val language: String?,
    val behaviorHints: BehaviorHints?,
    val source: StreamSource?,
    val extra: Set<StreamExtra>?
)

enum class StreamSource {
    LOW_QUALITY,
    MEDIUM_QUALITY,
    HIGH_QUALITY,
    MASTER_QUALITY,
    WORST_QUALITY,
    UNKNOWN
}

enum class StreamQuality {
    P_480(),
    P_720(),
    P_1080(),
    P_2160(),
    UNKNOWN
}

enum class StreamExtra(val isAvailable: Boolean) {
    FOUR_K(false),
    FIVE_POINT_ONE(false),
    SEVEN_POINT_ONE(false),
    HDR(false),
    DOLBY_VISION(false),
    DOLBY_DIGITAL(false),
    DOLBY_DIGITAL_PLUS(false),
    ATMOS(false),
    ATMOS_VISION(false),
    IMAX(false),
    IMAX_ENHANCED(false),
    HDR10(false),
    HDR10_PLUS(false),
    TRUE_HD(false),
    DTS(false),
    DTS_X(false),
}


data class BehaviorHints(
    val bingeGroup: String?,
    val notWebReady: Boolean?,
    val proxyHeaders: Map<String, String>?
)