package com.example.seteasecloudmusic.feature.home.data

import com.example.seteasecloudmusic.feature.mine.data.PlaylistDetailResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 首页推荐相关网络接口（每日推荐歌曲、推荐歌单、歌单详情等）
 */
interface DailyRecommendService {
    @GET("recommend/songs")
    suspend fun getDailyRecommendSongs(
        @Query("afresh") afresh: Boolean = false
    ): DailyRecommendSongsResponse

    /**
     * 获取每日推荐歌单（需要登录）
     */
    @GET("recommend/resource")
    suspend fun getRecommendResource(): RecommendResourceResponse

    /**
     * 获取指定歌单详情（含歌曲列表）
     */
    @GET("playlist/detail")
    suspend fun getPlaylistDetail(
        @Query("id") id: Long
    ): PlaylistDetailResponse

    /**
     * 获取 APP 首页模块信息（包括 HOMEPAGE_BLOCK_STYLE_RCMD 风格推荐模块）
     */
    @GET("homepage/block/page")
    suspend fun getHomepageBlockPage(
        @Query("refresh") refresh: Boolean = false,
        @Query("cursor") cursor: String? = null
    ): HomepageBlockPageResponse
}
