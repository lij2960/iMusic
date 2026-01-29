package com.ijackey.iMusic.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// 简化的测试接口
interface SimpleApi {
    @GET("search")
    suspend fun searchMusic(
        @Query("keywords") keywords: String,
        @Query("type") type: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<SimpleSearchResponse>
    
    @GET("lyric")
    suspend fun getLyrics(
        @Query("id") id: Long
    ): Response<SimpleLyricsResponse>
}

data class SimpleSearchResponse(
    val result: SimpleSearchResult?
)

data class SimpleSearchResult(
    val songs: List<SimpleSong>?
)

data class SimpleSong(
    val id: Long,
    val name: String,
    val ar: List<SimpleArtist>?,
    val al: SimpleAlbum?
)

data class SimpleArtist(
    val name: String
)

data class SimpleAlbum(
    val name: String,
    val picUrl: String?
)

data class SimpleLyricsResponse(
    val lrc: SimpleLrc?
)

data class SimpleLrc(
    val lyric: String?
)