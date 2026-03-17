package com.example.seteasecloudmusic.domain.repository

import com.example.seteasecloudmusic.domain.model.Track

interface MusicRepository {
    //搜索歌曲，返回匹配的曲目列表。
    suspend fun searchTracks(
        query: String,
        limit: Int = 30,
        offset: Int = 0
    ): Result<List<Track>>

    //获取指定歌曲的可播放 URL。
    suspend fun getTrackUrl(
        trackId: Long,
        level: String = "hires"
    ): Result<String>
}