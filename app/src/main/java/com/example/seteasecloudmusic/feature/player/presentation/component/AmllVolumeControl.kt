package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AmllVolumeControl(
    volume: Float,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 静音喇叭图标
        Icon(
            imageVector = if (volume <= 0f) Icons.Default.VolumeMute else Icons.Default.VolumeDown,
            contentDescription = "音量",
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )

        // 音量滑块
        AmllBouncingSlider(
            value = volume,
            max = 1f,
            isPlaying = false,
            onSeek = { newValue -> onChange(newValue) },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )

        // 大音量喇叭图标
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "音量",
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}
