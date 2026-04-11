package com.example.seteasecloudmusic.feature.search.domain

/**
 * `domain.usecase` 模块说明：
 *
 * UseCase 用来承载"单一业务动作"，把界面层想做的一件事包装起来。
 * 这样上层不需要知道数据来自哪里，也不需要自己拼接业务规则。
 *
 * `GetSearchSuggestionsUseCase` 负责：
 * 1. 接收搜索关键词和可选类型参数。
 * 2. 做最基础的输入校验与清洗。
 * 3. 调用 `MusicRepository` 返回搜索建议（包含单曲、歌手、歌单）。
 */
class GetSearchSuggestionsUseCase(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke(
        query: String,
        type: String? = null
    ): Result<SearchSuggestions> {
        if (query.isBlank()) {
            return Result.success(SearchSuggestions())
        }
        return searchRepository.getSearchSuggestions(query = query.trim(), type = type)
    }
}