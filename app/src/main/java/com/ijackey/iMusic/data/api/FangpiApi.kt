package com.ijackey.iMusic.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface FangpiApi {
    @GET("s/{keyword}")
    suspend fun searchMusic(@Path("keyword", encoded = true) keyword: String): Response<String>
    
    @GET("music/{id}")
    suspend fun getMusicDetail(@Path("id") id: String): Response<String>
    
    @GET
    suspend fun downloadMusic(@Url url: String): Response<okhttp3.ResponseBody>
}

data class FangpiTrack(
    val id: String,
    val title: String,
    val artist: String,
    val downloadUrl: String? = null,
    val detailUrl: String
)