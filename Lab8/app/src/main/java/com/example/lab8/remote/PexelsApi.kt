package com.example.lab8.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PexelsApi {
    @GET("curated")
    suspend fun getCurated(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): Response<PexelsResponse>

    @GET("search")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): Response<PexelsResponse>

    @GET("photos/{id}")
    suspend fun getPhotoById(@Path("id") id: Long): Response<PexelsPhoto>
}