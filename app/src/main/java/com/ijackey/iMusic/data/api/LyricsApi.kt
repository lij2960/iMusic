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

// QQ Music API
interface QQMusicApi {
    @GET("search")
    suspend fun searchQQMusic(
        @Query("key") keywords: String,
        @Query("pageNo") pageNo: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<QQMusicResponse>
}

// KuWo Music API  
interface KuWoMusicApi {
    @GET("search")
    suspend fun searchKuWoMusic(
        @Query("key") keywords: String,
        @Query("pn") pageNo: Int = 1,
        @Query("rn") pageSize: Int = 20
    ): Response<KuWoMusicResponse>
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

// QQ Music data models
data class QQMusicResponse(
    val data: QQMusicData?
)

data class QQMusicData(
    val song: QQSongData?
)

data class QQSongData(
    val list: List<QQTrack>?
)

data class QQTrack(
    val songname: String,
    val singer: List<QQSinger>,
    val albumname: String,
    val albummid: String
)

data class QQSinger(
    val name: String
)

// KuWo Music data models
data class KuWoMusicResponse(
    val data: KuWoMusicData?
)

data class KuWoMusicData(
    val list: List<KuWoTrack>?
)

data class KuWoTrack(
    val name: String,
    val artist: String,
    val album: String,
    val pic: String?
)