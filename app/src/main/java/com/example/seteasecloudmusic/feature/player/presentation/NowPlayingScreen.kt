package com.example.seteasecloudmusic.feature.player.presentation

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.example.seteasecloudmusic.core.player.PlaybackState
import com.example.seteasecloudmusic.core.player.PlayerStatus
import com.example.seteasecloudmusic.feature.player.presentation.component.AmllBouncingSlider
import com.example.seteasecloudmusic.feature.player.presentation.component.AmllControlThumb
import com.example.seteasecloudmusic.feature.player.presentation.component.AmllCover
import com.example.seteasecloudmusic.feature.player.presentation.component.AmllMediaControls
import com.example.seteasecloudmusic.feature.player.presentation.component.AmllMusicInfo
import com.example.seteasecloudmusic.feature.player.presentation.component.AmllToggleRow
import com.example.seteasecloudmusic.feature.player.presentation.component.AmllVerticalLayout
import com.example.seteasecloudmusic.feature.player.presentation.component.AmllVolumeControl
import com.example.seteasecloudmusic.feature.player.presentation.component.LyricsScreen
import com.example.seteasecloudmusic.feature.player.presentation.component.MusicInfoStyle
import com.example.seteasecloudmusic.feature.player.presentation.component.PlayerBackground
import com.example.seteasecloudmusic.feature.player.presentation.component.plusLighter
import com.example.seteasecloudmusic.feature.player.presentation.component.topFadeMask

@Composable
fun NowPlayingScreen(
    playbackState: PlaybackState,
    lyricsState: LyricsUiState,
    currentPosition: Int,
    activeLineIndex: Int,
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onFavoriteClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onKaraokeClick: () -> Unit = {},
    onQueueClick: () -> Unit = {},
    onAudioOutputClick: () -> Unit = {}
) {
    val currentTrack = playbackState.currentTrack
    val isPlaying = playbackState.status == PlayerStatus.PLAYING
    val duration = playbackState.durationMs.coerceAtLeast(1)
    val position = playbackState.currentPositionMs.coerceIn(0, duration)

    // AMLL 核心状态: 默认显示封面(隐藏歌词)
    var hideLyric by rememberSaveable { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 30f) onClose()
                }
            }
    ) {
        // ===== Layer 0: 背景 (保留) =====
        PlayerBackground(
            coverUrl = currentTrack?.coverUrl,
            isLyricsPage = !hideLyric
        )

        // ===== Layer 1: AmllVerticalLayout 主网格 =====
        AmllVerticalLayout(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            hideLyric = hideLyric,
            thumbSlot = {
                AmllControlThumb(onTap = onClose)
            },
            smallControlsSlot = {
                AmllMusicInfo(
                    track = currentTrack,
                    style = MusicInfoStyle.Compact,
                    onMore = onMoreClick,
                    modifier = Modifier.plusLighter()
                )
            },
            lyricSlot = {
                LyricsScreen(
                    lyricsState = lyricsState,
                    currentPosition = currentPosition,
                    activeLineIndex = activeLineIndex,
                    isPlaying = isPlaying,
                    onLineClick = onSeekTo,
                    modifier = Modifier
                        .topFadeMask(0.10f)
                        .plusLighter()
                )
            },
            bigControlsSlot = {
                Column {
                    AmllMusicInfo(
                        track = currentTrack,
                        style = MusicInfoStyle.Big,
                        onMore = onMoreClick,
                        modifier = Modifier.plusLighter()
                    )

                    AmllBouncingSlider(
                        value = position.toFloat(),
                        max = duration.toFloat(),
                        isPlaying = isPlaying,
                        onSeek = { newValue -> onSeekTo(newValue.toInt()) },
                        modifier = Modifier.plusLighter()
                    )

                    AmllMediaControls(
                        isPlaying = isPlaying,
                        onPrev = onPrevious,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        onShuffle = {},
                        onRepeat = {}
                    )

                    AmllToggleRow(
                        lyricsChecked = !hideLyric,
                        onLyricsClick = { hideLyric = !hideLyric },
                        onAirPlay = onAudioOutputClick,
                        onPlaylist = onQueueClick
                    )

                    AmllVolumeControl(
                        volume = 1f,
                        onChange = {}
                    )
                }
            },
            coverSlot = {
                AmllCover(
                    coverUrl = currentTrack?.coverUrl,
                    musicPaused = !isPlaying
                )
            }
        )
    }
}
