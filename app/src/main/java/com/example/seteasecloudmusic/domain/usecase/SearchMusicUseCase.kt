package com.example.seteasecloudmusic.domain.usecase

import com.example.seteasecloudmusic.domain.model.Track
import com.example.seteasecloudmusic.domain.repository.MusicRepository

/**
 * 搜索音乐的业务逻辑单元
 *
 * 1. 接收 ViewModel 传来的参数
 * 2. 调用 Repository 获取数据
 */

class SearchMusicUseCase(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(query: String): Result<List<Track>> {
        if (query.isBlank()) return Result.success(emptyList())
        return musicRepository.searchTracks(query)
    }
}