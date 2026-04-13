package com.example.seteasecloudmusic.feature.player.presentation

import androidx.lifecycle.ViewModel
import com.example.seteasecloudmusic.core.model.Track
import com.example.seteasecloudmusic.core.player.MusicPlayerController
import com.example.seteasecloudmusic.core.player.PlaybackState
import com.example.seteasecloudmusic.core.player.PlayerStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * 播放器 ViewModel：
 * 1) 对 UI 暴露只读播放状态
 * 2) 转发播放控制命令
 * 3) 统一处理 connect/release 生命周期入口
 */
class PlayerViewModel(
    private val controller: MusicPlayerController
) : ViewModel(){

    // 直接复用 Controller 内部的状态流，避免重复维护一份状态
    val playbackState: StateFlow<PlaybackState> = controller.playbackState

    /** 建立与 MusicService 的连接 */
    fun connect() {
        controller.connect()
    }

    fun onPlayPause() {
        when(playbackState.value.status) {
            PlayerStatus.PLAYING -> controller.pause()
            PlayerStatus.PAUSED -> controller.pause()
            PlayerStatus.BUFFERING -> Unit
            PlayerStatus.IDLE,
            PlayerStatus.ENDED,
            PlayerStatus.ERROR ->  controller.replayCurrent()
        }
    }

    fun onNext() {
        controller.playNext()
    }

    fun onPrevious() {
        controller.playPrevious()
    }

    fun seekTo(positionMs: Int) {
        controller.seekTo(positionMs)
    }

    override  fun onCleared() {
        // ViewModel 销毁时释放控制器，避免监听器和协程泄漏
        controller.release()
        super.onCleared()
    }
}