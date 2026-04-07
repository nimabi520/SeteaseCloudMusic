package com.example.seteasecloudmusic.player.playback

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.seteasecloudmusic.domain.model.Track
import com.example.seteasecloudmusic.domain.usecase.PrepareTrackForPlaybackUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class PlayerStatus {
    IDLE,      // 初始或已停止，无有效播放任务
    BUFFERING, // 正在准备播放资源
    PLAYING,   // 正在播放
    PAUSED,    // 已暂停，可继续
    ENDED,     // 自然播放结束
    ERROR      // 播放链路发生错误
}

data class PlaybackState(
    val status: PlayerStatus = PlayerStatus.IDLE,
    val currentTrack: Track? = null, // 当前要播/正在播的曲目
    val currentPositionMs: Int = 0,  // 当前进度（毫秒）
    val durationMs: Int = 0,         // 总时长（毫秒），未知时为 0
    val errorMessage: String? = null // 最近一次错误信息（仅 ERROR 态有值）
)

class MusicPlayerController(
    private val prepareTrackForPlaybackUseCase: PrepareTrackForPlaybackUseCase,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val scope = CoroutineScope(SupervisorJob() + mainDispatcher)

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun play(track: Track) {
        scope.launch {
            // 进入缓冲态：UI 可显示加载指示器，且清掉历史错误。
            _playbackState.update {
                it.copy(
                    status = PlayerStatus.BUFFERING,
                    currentTrack = track,
                    errorMessage = null
                )
            }

            // URL 获取属于 I/O 操作，切到 ioDispatcher，避免阻塞主线程。
            val preparedResult = kotlinx.coroutines.withContext(ioDispatcher) {
                prepareTrackForPlaybackUseCase(track)
            }

            preparedResult
                .onSuccess { preparedTrack ->
                    // 双重校验：既要有 URL，也要业务上允许播放。
                    val url = preparedTrack.playableUrl
                    if (url.isNullOrBlank() || !preparedTrack.isPlayable) {
                        _playbackState.update {
                            it.copy(
                                status = PlayerStatus.ERROR,
                                errorMessage = "Track is not playable"
                            )
                        }
                        return@onSuccess
                    }
                    startMediaPlayer(preparedTrack, url)
                }
                .onFailure { throwable ->
                    // 用例失败统一转为 ERROR 态，交给 UI 处理提示。
                    _playbackState.update {
                        it.copy(
                            status = PlayerStatus.ERROR,
                            errorMessage = throwable.message ?: "Unknown playback error"
                        )
                    }
                }
        }
    }

    fun pause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _playbackState.update {
                it.copy(
                    status = PlayerStatus.PAUSED,
                    currentPositionMs = safeCurrentPosition(player)
                )
            }
        }
    }

    fun resume() {
        val player = mediaPlayer ?: return
        runCatching { player.start() }
            .onSuccess {
                _playbackState.update { it.copy(status = PlayerStatus.PLAYING) }
                startProgressTicker()
            }
            .onFailure { e ->
                _playbackState.update {
                    it.copy(status = PlayerStatus.ERROR, errorMessage = e.message)
                }
            }
    }

    fun stop() {
        stopInternal(resetState = true)
    }

    fun seekTo(positionMs: Int) {
        val player = mediaPlayer ?: return
        runCatching { player.seekTo(positionMs) }
    }

    fun release() {
        stopInternal(resetState = true)
        scope.coroutineContext.cancel()
    }

    private fun startMediaPlayer(track: Track, url: String) {
        // 启新播放前先清理旧播放器，避免资源泄漏和多实例并发播放。
        stopInternal(resetState = false)

        val player = MediaPlayer()
        mediaPlayer = player

        runCatching {
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            player.setDataSource(url)

            player.setOnPreparedListener { mp ->
                // prepareAsync 完成后启动播放，并写入初始时长/进度。
                mp.start()
                _playbackState.update {
                    it.copy(
                        status = PlayerStatus.PLAYING,
                        currentTrack = track,
                        durationMs = runCatching { mp.duration }.getOrDefault(0),
                        currentPositionMs = 0,
                        errorMessage = null
                    )
                }
                startProgressTicker()
            }

            player.setOnCompletionListener {
                // 自然结束：停止进度轮询并切 ENDED 态。
                stopProgressTicker()
                _playbackState.update {
                    it.copy(
                        status = PlayerStatus.ENDED,
                        currentPositionMs = it.durationMs
                    )
                }
            }

            player.setOnErrorListener { _, what, extra ->
                // MediaPlayer 底层错误统一收敛到 ERROR 态，避免静默失败。
                stopProgressTicker()
                _playbackState.update {
                    it.copy(
                        status = PlayerStatus.ERROR,
                        errorMessage = "MediaPlayer error what=$what extra=$extra"
                    )
                }
                true
            }

            // 异步准备，避免主线程同步 prepare 导致卡顿。
            player.prepareAsync()
        }.onFailure { e ->
            // 初始化失败时同步回收播放器，防止半初始化对象残留。
            _playbackState.update {
                it.copy(status = PlayerStatus.ERROR, errorMessage = e.message)
            }
            stopInternal(resetState = false)
        }
    }

    private fun startProgressTicker() {
        stopProgressTicker()
        progressJob = scope.launch {
            while (isActive) {
                val player = mediaPlayer ?: break
                _playbackState.update {
                    it.copy(
                        currentPositionMs = safeCurrentPosition(player),
                        durationMs = safeDuration(player)
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

    private fun stopInternal(resetState: Boolean) {
        stopProgressTicker()
        mediaPlayer?.let { player ->
            // 逐步释放：stop/reset/release 都包 runCatching，避免状态异常崩溃。
            runCatching { player.stop() }
            runCatching { player.reset() }
            runCatching { player.release() }
        }
        mediaPlayer = null

        // resetState=true 时回到初始态（用于 stop/release）。
        if (resetState) {
            _playbackState.value = PlaybackState()
        }
    }

    private fun safeCurrentPosition(player: MediaPlayer): Int {
        return runCatching { player.currentPosition }.getOrDefault(0)
    }

    private fun safeDuration(player: MediaPlayer): Int {
        return runCatching { player.duration }.getOrDefault(0)
    }
}