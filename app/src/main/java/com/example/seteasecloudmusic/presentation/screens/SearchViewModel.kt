package com.example.seteasecloudmusic.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seteasecloudmusic.domain.usecase.SearchMusicUseCase
import com.example.seteasecloudmusic.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val tracks: List<Track> = emptyList(),
    val errorMessage: String? = null
)

class SearchViewModel(private val searchMusicUseCase: SearchMusicUseCase) : ViewModel {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()
    private var lastSubmittedQuery: String? = null
    private var searchJob: Job? = null

    fun onQueryChanged(newQuery: String) {
        if (newQuery.isBlank()){
            current.copy(
                query = newQuery,
                tracks = emptyList(),
                errorMessage = null
                hasSearched = false
            )
        } else {
            current.copy(
                query = newQuery,
                errorMessage = null)
        }
    }

    fun onSearchSubmit(){
        val query = uiState.value.query.trim()
        if (query.isEmpty()) {
            return
        }
        lastSubmittedQuety = query
        search(query)
    }

    private fun search(query: String) {
        // 1) 先取消上一次搜索，避免“旧请求后返回”把新结果覆盖掉。
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            // 2) 发请求前先进入加载态，并清空旧错误。
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            // 3) 调用 UseCase。它会返回 Result<List<Track>>。
            val result = searchMusicUseCase(query)

            // 4) 按成功/失败分别更新 UI 状态。
            result.fold(
                onSuccess = { tracks ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            tracks = tracks,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            tracks = emptyList(),
                            errorMessage = throwable.message ?: "搜索失败，请稍后重试"
                        )
                    }
                }
            )
        }
    }

    fun onRetryClick(){
        // 正在搜索时不重复发起请求，避免并发重试。
        if (uiState.value.isLoading) {
            return
        }

        // 优先使用上次提交的关键词；若为空则回退到当前输入框内容。
        val retryQuery = lastSubmittedQuery
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: uiState.value.query.trim()

        // 仍为空时给出提示，不发请求。
        if (retryQuery.isEmpty()) {
            _uiState.update { state ->
                state.copy(errorMessage = "没有可重试的关键词")
            }
            return
        }

        // 记录本次重试关键词，并复用统一搜索流程。
        lastSubmittedQuery = retryQuery
        search(retryQuery)
    }

    fun onClearQuery(){
        // 1) 清空时先取消正在进行的请求，避免返回结果覆盖清空后的页面状态。
        searchJob?.cancel()
        searchJob = null

        // 2) 重置“可重试关键词”，确保后续重试行为与当前输入一致。
        lastSubmittedQuery = null

        // 3) 回到初始 UI 状态：输入为空、非加载、无结果、无错误。
        _uiState.update { state ->
            state.copy(
                query = "",
                isLoading = false,
                tracks = emptyList(),
                errorMessage = null
            )
        }
    }



}