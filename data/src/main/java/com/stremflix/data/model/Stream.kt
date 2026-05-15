package com.stremflix.data.model

data class Stream(
    val description: String?,
    val url: String,
    val quality: String?,
    val language: String?,
    val behaviorHints: BehaviorHints?
)

data class BehaviorHints(
    val bingeGroup: String?,
    val notWebReady: Boolean?,
    val proxyHeaders: Map<String, String>?
)