package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AmllMediaControls(
    isPlaying: Boolean,
    onPrev: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp), // 2vh min(2vw, 2vh)
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle 按钮
        AmllMediaButton(
            icon = Icons.Default.Shuffle,
            contentDescription = "随机播放",
            onClick = onShuffle,
            modifier = Modifier.size(48.dp),
            iconSize = 24.dp,
            tint = Color.White.copy(alpha = 0.7f)
        )

        // 上一首按钮
        AmllMediaButton(
            icon = Icons.Default.FastRewind,
            contentDescription = "上一首",
            onClick = onPrev,
            modifier = Modifier.size(48.dp),
            iconSize = 24.dp,
            tint = Color.White
        )

        // 播放/暂停按钮 (中央, 较大)
        AmllMediaButton(
            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "暂停" else "播放",
            onClick = onPlayPause,
            modifier = Modifier.size(56.dp),
            iconSize = 32.dp,
            tint = Color.White,
            isPlayButton = true
        )

        // 下一首按钮
        AmllMediaButton(
            icon = Icons.Default.FastForward,
            contentDescription = "下一首",
            onClick = onNext,
            modifier = Modifier.size(48.dp),
            iconSize = 24.dp,
            tint = Color.White
        )

        // Repeat 按钮
        AmllMediaButton(
            icon = Icons.Default.Repeat,
            contentDescription = "循环播放",
            onClick = onRepeat,
            modifier = Modifier.size(48.dp),
            iconSize = 24.dp,
            tint = Color.White.copy(alpha = 0.7f)
        )
    }
}
