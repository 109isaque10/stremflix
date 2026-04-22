package com.stremflix.data.network

import com.stremflix.data.network.dto.TmdbCreditsResponse
import com.stremflix.data.network.dto.TmdbPagedResponse
import com.stremflix.data.network.dto.TmdbVideosResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbService {
    @GET("3/trending/all/day")
    suspend fun trending(
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): TmdbPagedResponse

    @GET("3/movie/popular")
    suspend fun popularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): TmdbPagedResponse

    @GET("3/search/multi")
    suspend fun search(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("query") query: String
    ): TmdbPagedResponse

    @GET("3/{type}/{id}/videos")
    suspend fun videos(
        @Path("type") type: String,
        @Path("id") id: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): TmdbVideosResponse

    @GET("3/{type}/{id}/credits")
    suspend fun credits(
        @Path("type") type: String,
        @Path("id") id: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): TmdbCreditsResponse
}
