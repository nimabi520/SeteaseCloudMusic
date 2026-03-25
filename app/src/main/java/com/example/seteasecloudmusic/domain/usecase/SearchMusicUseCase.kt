package com.example.seteasecloudmusic.domain.usecase

import com.example.seteasecloudmusic.domain.model.Track
import com.example.seteasecloudmusic.domain.repository.MusicRepository

/**
 * `domain.usecase` 模块说明：
 *
 * UseCase 用来承载“单一业务动作”，把界面层想做的一件事包装起来。
 * 这样上层不需要知道数据来自哪里，也不需要自己拼接业务规则。
 *
 * `SearchMusicUseCase` 负责：
 * 1. 接收搜索关键词和分页参数。
 * 2. 做最基础的输入校验与清洗。
 * 3. 调用 `MusicRepository` 返回搜索结果。
 */
class SearchMusicUseCase(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(
        query: String,
        limit: Int = 30,
        offset: Int = 0
    ): Result<List<Track>> {
        if (query.isBlank()) return Result.success(emptyList())
        return musicRepository.searchTracks(query = query.trim(), limit = limit, offset = offset)
    }
}
