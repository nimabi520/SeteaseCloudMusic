package com.example.seteasecloudmusic.data.api

import com.example.seteasecloudmusic.data.model.SearchResultDao
import com.example.seteasecloudmusic.data.model.SongDao
import retrofit2.http.GET
import retrofit2.http.Query

interface NeteaseMusicService {

    @GET("song/url/v1")
    suspend fun getSongUrl(
        @Query("id") id: Long,
        @Query("level") level: String = "hires"
    ): SongDao

    @GET("cloudsearch")
    suspend fun searchSongs(
        @Query("keywords") keywords: String,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0,
        @Query("type") type: Int = SearchType.SONG.value
    ): SearchResultDao

    @GET("search")
    suspend fun searchSongsLite(
        @Query("keywords") keywords: String,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0,
        @Query("type") type: Int = SearchType.SONG.value
    ): SearchResultDao
}

enum class SearchType(val value: Int) {
    SONG(1),
    ALBUM(10),
    ARTIST(100),
    PLAYLIST(1000),
    USER(1002),
    MV(1004),
    LYRIC(1006),
    RADIO(1009),
    VIDEO(1014),
    COMPOSITE(1018),
    VOICE(2000)
}