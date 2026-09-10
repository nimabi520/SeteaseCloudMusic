package com.example.seteasecloudmusic.feature.home.domain.repository

import com.example.seteasecloudmusic.core.model.Track

data class RadarPlaylistData(
    val id: Long,
    val name: String,
    val description: String?,
    val coverUrl: String?,
    val trackCount: Int,
    val tracks: List<Track>
)

data class FavoriteRecommendSectionData(
    val title: String = "根据你喜爱的歌曲推荐",
    val tracks: List<Track> = emptyList()
)

interface HomeRecommendRepository {
    suspend fun getDailyRecommendSongs(
        afresh: Boolean = false
    ): Result<List<Track>>

    fun getCachedDailyRecommendSongs(): List<Track>?

    /**
     * 获取雷达歌单（含歌曲列表）
     */
    suspend fun getRadarPlaylist(): Result<RadarPlaylistData>

    /**
     * 获取本地缓存的雷达歌单
     */
    fun getCachedRadarPlaylist(): RadarPlaylistData?

    /**
     * 获取首页「根据你喜爱的歌曲推荐」（来自 /homepage/block/page 的 HOMEPAGE_BLOCK_STYLE_RCMD）
     */
    suspend fun getFavoriteRecommendSongs(
        afresh: Boolean = false
    ): Result<FavoriteRecommendSectionData>

    /**
     * 获取本地缓存的喜好风格推荐歌曲
     */
    fun getCachedFavoriteRecommendSongs(): FavoriteRecommendSectionData?

    /**
     * 获取用户「我喜欢的音乐」最新红心单曲封面（供心动模式卡片使用）
     */
    suspend fun getUserLikedMusicCover(): String?
}
