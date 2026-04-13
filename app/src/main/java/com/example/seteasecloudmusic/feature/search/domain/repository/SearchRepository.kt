package com.example.seteasecloudmusic.feature.search.domain

import com.example.seteasecloudmusic.core.model.Track

interface SearchRepository {
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

    //获取搜索建议，返回单曲、歌手、歌单的综合建议。
    suspend fun getSearchSuggestions(
        query: String,
        type: String? = null
    ): Result<SearchSuggestions>
}