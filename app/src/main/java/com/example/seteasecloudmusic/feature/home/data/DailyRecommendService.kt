package com.example.seteasecloudmusic.feature.home.data

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 每日推荐歌曲接口。
 *
 * 注意：登录 Cookie 由全局 AuthInterceptor 自动附加，
 * 这里不手动声明 Cookie Header。
 */
interface DailyRecommendService {
    @GET("recommend/songs")
    suspend fun getDailyRecommendSongs(
        @Query("afresh") afresh: Boolean = false
    ): DailyRecommendSongsResponse
}
