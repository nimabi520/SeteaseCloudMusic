package com.example.seteasecloudmusic.feature.player.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun PlayerRoute(
    viewModel: PlayerViewModel,
    onClose: () -> Unit
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val lyricsState by viewModel.lyricsState.collectAsState()
    val currentPosition by viewModel.currentPositionMs.collectAsState()
    val activeLineIndex by viewModel.activeLineIndex.collectAsState()

    NowPlayingScreen(
        playbackState = playbackState,
        lyricsState = lyricsState,
        currentPosition = currentPosition,
        activeLineIndex = activeLineIndex,
        onClose = onClose,
        onPlayPause = viewModel::onPlayPause,
        onNext = viewModel::onNext,
        onPrevious = viewModel::onPrevious,
        onSeekTo = viewModel::seekTo
    )
}