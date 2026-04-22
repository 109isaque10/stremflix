package com.stremflix.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class StremioManifestDto(
    val id: String,
    val name: String,
    val resources: List<String> = emptyList()
)

@Serializable
data class StremioStreamsResponse(
    val streams: List<StremioStreamDto> = emptyList()
)

@Serializable
data class StremioStreamDto(
    val name: String? = null,
    val title: String? = null,
    val url: String? = null,
    val ytId: String? = null,
    val behaviorHints: BehaviorHints? = null,
    val subtitles: List<StremioSubtitleDto> = emptyList()
) {
    @Serializable
    data class BehaviorHints(
        val notWebReady: Boolean? = null,
        val proxyHeaders: ProxyHeaders? = null
    )

    @Serializable
    data class ProxyHeaders(
        val request: Map<String, String>? = null
    )
}

@Serializable
data class StremioSubtitleDto(
    val lang: String,
    val url: String
)
