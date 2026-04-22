package com.stremflix.data.network

import retrofit2.http.GET
import retrofit2.http.Header

interface TraktService {
    @GET("users/me/lists")
    suspend fun lists(@Header("trakt-api-key") key: String): List<TraktListDto>
}

data class TraktListDto(val name: String)
