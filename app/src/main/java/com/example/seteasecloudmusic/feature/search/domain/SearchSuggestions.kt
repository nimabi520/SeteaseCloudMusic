package com.example.seteasecloudmusic.feature.search.domain

import com.example.seteasecloudmusic.core.model.Track

/**
 * 搜索建议结果，包含单曲、歌手、歌单建议。
 */
data class SearchSuggestions(
    val songs: List<Track> = emptyList(),
    val artists: List<ArtistSuggestion> = emptyList(),
    val playlists: List<PlaylistSuggestion> = emptyList(),
    val allMatch: List<String> = emptyList()
)

/**
 * 歌手建议项。
 */
data class ArtistSuggestion(
    val id: Long,
    val name: String,
    val coverUrl: String?
)

/**
 * 歌单建议项。
 */
data class PlaylistSuggestion(
    val id: Long,
    val name: String,
    val coverUrl: String?,
    val trackCount: Int
)