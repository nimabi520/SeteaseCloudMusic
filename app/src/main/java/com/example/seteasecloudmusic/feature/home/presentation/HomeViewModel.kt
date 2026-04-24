package com.example.seteasecloudmusic.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seteasecloudmusic.core.model.Track
import com.example.seteasecloudmusic.core.player.MusicPlayerController
import com.example.seteasecloudmusic.feature.home.domain.usecase.GetDailyRecommendSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val tracks: List<Track> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDailyRecommendSongsUseCase: GetDailyRecommendSongsUseCase,
    private val musicPlayerController: MusicPlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var playbackJob: Job? = null

    init {
        refreshDailyRecommend()
    }

    fun refreshDailyRecommend(afresh: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = getDailyRecommendSongsUseCase(afresh)
            result.fold(
                onSuccess = { tracks ->
                    _uiState.update {
                        it.copy(
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
                            errorMessage = throwable.message ?: "获取每日推荐失败"
                        )
                    }
                }
            )
        }
    }

    fun onRetryClick() {
        refreshDailyRecommend(afresh = false)
    }

    fun onRefreshClick() {
        refreshDailyRecommend(afresh = true)
    }

    fun onTrackClick(track: Track) {
        val snapshotTracks = uiState.value.tracks.distinctBy { it.id }
        if (snapshotTracks.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "暂无可播放的每日推荐") }
            return
        }

        val clickedIndex = snapshotTracks.indexOfFirst { it.id == track.id }
        if (clickedIndex !in snapshotTracks.indices) {
            _uiState.update { it.copy(errorMessage = "未找到所选歌曲") }
            return
        }

        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            musicPlayerController.replaceQueueAndPlay(snapshotTracks, clickedIndex)
        }
    }
}
