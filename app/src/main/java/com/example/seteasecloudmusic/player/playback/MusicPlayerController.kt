package com.example.seteasecloudmusic.player.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.seteasecloudmusic.domain.model.Track
import com.example.seteasecloudmusic.domain.usecase.PrepareTrackForPlaybackUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class PlayerStatus { IDLE, BUFFERING, PLAYING, PAUSED, ENDED, ERROR }

data class PlaybackState(
    val status: PlayerStatus = PlayerStatus.IDLE,
    val currentTrack: Track? = null,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val errorMessage: String? = null
)

class MusicPlayerController(
    private val context: Context,
    private val prepareTrackForPlaybackUseCase: PrepareTrackForPlaybackUseCase,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    // 控制器自己的协程域：用于异步连接服务、拉 URL、更新状态
    private val scope = CoroutineScope(SupervisorJob() + mainDispatcher)

    // Media3 控制端（连接到 MusicService 的 MediaSession）
    private var controller: MediaController? = null

    // 进度轮询任务：每 500ms 同步一次 position/duration 到 UI
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    // 监听 Media3 播放器状态变化，统一映射到你的 PlaybackState
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val c = controller ?: return
            val mapped = when (playbackState) {
                Player.STATE_IDLE -> PlayerStatus.IDLE
                Player.STATE_BUFFERING -> PlayerStatus.BUFFERING
                Player.STATE_READY -> if (c.isPlaying) PlayerStatus.PLAYING else PlayerStatus.PAUSED
                Player.STATE_ENDED -> PlayerStatus.ENDED
                else -> PlayerStatus.ERROR
            }

            _playbackState.update {
                it.copy(
                    status = mapped,
                    currentPositionMs = c.currentPosition.toInt().coerceAtLeast(0),
                    durationMs = c.duration.takeIf { d -> d > 0 }?.toInt() ?: 0
                )
            }

            if (mapped == PlayerStatus.PLAYING) startProgressTicker() else stopProgressTicker()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.update {
                it.copy(status = if (isPlaying) PlayerStatus.PLAYING else PlayerStatus.PAUSED)
            }
            if (isPlaying) startProgressTicker() else stopProgressTicker()
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            stopProgressTicker()
            _playbackState.update {
                it.copy(status = PlayerStatus.ERROR, errorMessage = error.message ?: "Playback error")
            }
        }
    }

    /** 在 ViewModel 初始化时调用：建立到 MusicService 的连接 */
    fun connect() {
        val token = SessionToken(context, ComponentName(context, MusicService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()

        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess { c ->
                        controller = c
                        c.addListener(playerListener)
                    }
                    .onFailure { e ->
                        _playbackState.update {
                            it.copy(status = PlayerStatus.ERROR, errorMessage = e.message)
                        }
                    }
            },
            context.mainExecutor
        )
    }

    fun play(track: Track) {
        scope.launch {
            _playbackState.update {
                it.copy(status = PlayerStatus.BUFFERING, currentTrack = track, errorMessage = null)
            }

            val prepared = withContext(ioDispatcher) { prepareTrackForPlaybackUseCase(track) }

            prepared
                .onSuccess { t ->
                    val url = t.playableUrl
                    if (url.isNullOrBlank() || !t.isPlayable) {
                        _playbackState.update {
                            it.copy(status = PlayerStatus.ERROR, errorMessage = "Track is not playable")
                        }
                        return@onSuccess
                    }

                    val item = MediaItem.Builder()
                        .setMediaId(t.id.toString())
                        .setUri(url)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(t.title)
                                .setArtist(t.artists.joinToString(" / ") { it.name })
                                .setArtworkUri(t.coverUrl?.toUri())
                                .build()
                        )
                        .build()

                    controller?.apply {
                        setMediaItem(item)
                        prepare()
                        play()
                    } ?: _playbackState.update {
                        it.copy(status = PlayerStatus.ERROR, errorMessage = "Controller not connected")
                    }
                }
                .onFailure { e ->
                    _playbackState.update {
                        it.copy(status = PlayerStatus.ERROR, errorMessage = e.message ?: "Unknown error")
                    }
                }
        }
    }

    fun pause() = controller?.pause() ?: Unit
    fun resume() = controller?.play() ?: Unit
    fun stop() = controller?.stop() ?: Unit
    fun seekTo(positionMs: Int) = controller?.seekTo(positionMs.toLong()) ?: Unit

    fun release() {
        stopProgressTicker()
        controller?.removeListener(playerListener)
        controller?.release()
        controller = null
        scope.cancel()
    }

    private fun startProgressTicker() {
        stopProgressTicker()
        progressJob = scope.launch {
            while (isActive) {
                val c = controller ?: break
                _playbackState.update {
                    it.copy(
                        currentPositionMs = c.currentPosition.toInt().coerceAtLeast(0),
                        durationMs = c.duration.takeIf { d -> d > 0 }?.toInt() ?: 0
                    )
                }
                delay(500L)
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
    }
}
