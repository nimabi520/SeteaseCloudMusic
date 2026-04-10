package com.example.seteasecloudmusic.player.presentation

import androidx.lifecycle.ViewModel
import com.example.seteasecloudmusic.domain.model.Track
import com.example.seteasecloudmusic.player.playback.MusicPlayerController
import com.example.seteasecloudmusic.player.playback.PlaybackState
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

    /** 请求播放指定歌曲（内部会先准备 URL） */
    fun play(track: Track) {
        controller.play(track)
    }

    fun pause() {
        controller.pause()
    }

    fun resume() {
        controller.resume()
    }

    fun stop() {
        controller.stop()
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