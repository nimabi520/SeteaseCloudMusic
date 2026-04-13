package com.example.seteasecloudmusic.feature.player.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.dp
import com.example.seteasecloudmusic.core.player.PlaybackState
import com.example.seteasecloudmusic.core.player.PlayerStatus

@Composable
fun NowPlayingScreen(
    playbackState: PlaybackState,
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Int) -> Unit
) {
    val track = playbackState.currentTrack
    val isPlaying = playbackState.status == PlayerStatus.PLAYING
    val duration = playbackState.durationMs.coerceAtLeast(1)
    val position = playbackState.currentPositionMs.coerceIn(0, duration)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部:关闭按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "关闭")
            }
        }

        // 中部：歌曲信息
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = track?.title ?: "未知歌曲",
                style = MaterialTheme.typography.headlineMedium
            )
            // Track 没有 artist 字段，使用 artists 列表拼接展示
            Text(
                text = track?.artists?.joinToString(" / ") { it.name } ?: "未知歌手",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // 底部：进度 + 播放控制
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Slider(
                value = position.toFloat(),
                onValueChange = { onSeekTo(it.toInt()) },
                valueRange = 0f..duration.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "上一首")
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放"
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = onNext) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "下一首")
                }
            }
        }
    }
}