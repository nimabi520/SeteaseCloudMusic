package com.example.seteasecloudmusic.data.api

import com.example.seteasecloudmusic.data.model.SearchResultDao
import com.example.seteasecloudmusic.data.model.SongDao
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 网易云音乐核心网络接口：搜索与歌曲播放链接。
 */
interface NeteaseMusicService {
    /**
     * 根据歌曲 id 获取可播放 URL。
     */
    @GET("song/url/v1")
    suspend fun getSongUrl(
        @Query("id") id: Long,
        @Query("level") level: String = "hires"
    ): SongDao

    /**
     * 使用 cloudsearch 获取更完整的搜索结果。
     */
    @GET("cloudsearch")
    suspend fun searchSongs(
        @Query("keywords") keywords: String,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0,
        @Query("type") type: Int = SearchType.SONG.value
    ): SearchResultDao

    /**
     * 使用基础 search 接口获取搜索结果。
     */
    @GET("search")
    suspend fun searchSongsLite(
        @Query("keywords") keywords: String,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0,
        @Query("type") type: Int = SearchType.SONG.value
    ): SearchResultDao
}

/**
 * 搜索类型映射，值与后端接口约定保持一致。
 */
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