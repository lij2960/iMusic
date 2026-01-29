package com.ijackey.iMusic.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LyricsApi {
    @GET("lyric")
    suspend fun getLyrics(
        @Query("id") id: Long
    ): Response<LyricsResponse>
}

data class LyricsResponse(
    val lrc: LyricsContent?
)

data class LyricsContent(
    val lyric: String?
)

interface MusicSearchApi {
    @GET("search")
    suspend fun searchMusic(
        @Query("keywords") keywords: String,
        @Query("type") type: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<MusicSearchResponse>
}

data class MusicSearchResponse(
    val result: SearchResult?,
    val code: Int
)

data class SearchResult(
    val songs: List<OnlineTrack>?,
    val hasMore: Boolean?,
    val songCount: Int?
)

data class OnlineTrack(
    val id: Long,
    val name: String,
    val artists: List<Artist>,
    val album: Album
)

data class Artist(
    val name: String,
    val img1v1Url: String?
)

data class Album(
    val name: String,
    val picId: Long,
    val artist: AlbumArtist
)

data class AlbumArtist(
    val name: String,
    val img1v1Url: String?
)