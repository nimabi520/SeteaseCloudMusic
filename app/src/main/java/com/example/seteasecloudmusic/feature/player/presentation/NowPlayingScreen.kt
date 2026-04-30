package com.example.seteasecloudmusic.feature.player.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seteasecloudmusic.core.player.PlaybackState
import com.example.seteasecloudmusic.core.player.PlayerStatus
import com.example.seteasecloudmusic.feature.player.presentation.component.LyricBackground
import com.example.seteasecloudmusic.feature.player.presentation.component.LyricsScreen

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
    onSeekTo: (Int) -> Unit
) {
    val currentTrack = playbackState.currentTrack
    val isPlaying = playbackState.status == PlayerStatus.PLAYING
    val duration = playbackState.durationMs.coerceAtLeast(1)
    val position = playbackState.currentPositionMs.coerceIn(0, duration)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val bottomControlsHeight = (maxHeight * 0.24f).coerceAtLeast(196.dp)

        // 最底层：共享的动态流体背景
        LyricBackground(coverUrl = currentTrack?.coverUrl)

        // 中间层：歌词主视图（AMLL 风格）
        LyricsScreen(
            lyricsState = lyricsState,
            currentPosition = currentPosition,
            activeLineIndex = activeLineIndex,
            isPlaying = isPlaying,
            onLineClick = onSeekTo,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomControlsHeight + 12.dp)
        )

        // 顶层：信息与控制区
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 顶部：收起 + 曲目信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "收起",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentTrack?.title ?: "未知歌曲",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = currentTrack?.artists?.joinToString(" / ") { it.name } ?: "未知歌手",
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.58f),
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // 底部：播放控制区
            BottomControls(
                playbackState = playbackState,
                position = position,
                duration = duration,
                isPlaying = isPlaying,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
                onSeekTo = onSeekTo,
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 28.dp, vertical = 20.dp)
            )
        }
    }
}

@Composable
private fun BottomControls(
    playbackState: PlaybackState,
    position: Int,
    duration: Int,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Slider(
            value = position.toFloat(),
            onValueChange = { onSeekTo(it.toInt()) },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(position),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatTime(duration),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "上一首",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "下一首",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

private fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
