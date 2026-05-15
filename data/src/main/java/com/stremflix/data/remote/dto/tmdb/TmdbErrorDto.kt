// data/src/main/java/com/stremflix/data/remote/dto/tmdb/TmdbErrorDto.kt

package com.stremflix.data.remote.dto.tmdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbErrorDto(
    @SerialName("status_code") val statusCode: Int,
    @SerialName("status_message") val statusMessage: String,
    val success: Boolean
)

// Helper to check if response is an error
fun TmdbErrorDto.isError(): Boolean = !success || statusCode != 0