package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AmllToggleRow(
    lyricsChecked: Boolean,
    onLyricsClick: () -> Unit,
    onAirPlay: () -> Unit,
    onPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lyrics 切换按钮
        ToggleIconButton(
            checked = lyricsChecked,
            checkedIcon = Icons.Default.Lyrics,
            uncheckedIcon = Icons.Default.Lyrics,
            contentDescription = "歌词",
            onClick = onLyricsClick
        )

        // AirPlay 按钮
        ToggleIconButton(
            checked = false,
            checkedIcon = Icons.Default.AirplanemodeActive,
            uncheckedIcon = Icons.Default.AirplanemodeActive,
            contentDescription = "AirPlay",
            onClick = onAirPlay
        )

        // Playlist 切换按钮
        ToggleIconButton(
            checked = false,
            checkedIcon = Icons.Default.List,
            uncheckedIcon = Icons.Default.List,
            contentDescription = "播放列表",
            onClick = onPlaylist
        )
    }
}

@Composable
private fun ToggleIconButton(
    checked: Boolean,
    checkedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    uncheckedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp) // 3em ≈ 36dp
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (checked) checkedIcon else uncheckedIcon,
            contentDescription = contentDescription,
            tint = Color.White.copy(alpha = if (checked) 0.9f else 0.45f),
            modifier = Modifier.size(24.dp)
        )
    }
}
