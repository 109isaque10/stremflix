package com.stremflix.data.remote.dto.stremio

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StremioStreamResponse(
    val streams: List<StremioStreamDto>
)

@Serializable
data class StremioStreamDto(

    val description: String?,
    val url: String?,
    val name: String?,
    val title: String?,
    val behaviorHints: StremioBehaviorHintsDto?
) {
    val quality: String?
        get() = title?.substringBefore(" ") ?: name?.substringBefore(" ")
}

@Serializable
data class StremioBehaviorHintsDto(
    @SerialName("bingeGroup") val bingeGroup: String?,
    @SerialName("notWebReady") val notWebReady: Boolean?,
    @SerialName("proxyHeaders") val proxyHeaders: StremioProxyHeadersDto?
)

@Serializable
data class StremioProxyHeadersDto(
    val request: Map<String, String>?
)