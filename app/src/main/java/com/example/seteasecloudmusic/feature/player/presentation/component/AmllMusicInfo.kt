package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seteasecloudmusic.core.model.Track

enum class MusicInfoStyle {
    Compact, // 歌词模式小信息条
    Big      // 封面模式大信息
}

@Composable
fun AmllMusicInfo(
    track: Track?,
    style: MusicInfoStyle,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (track == null) return

    when (style) {
        MusicInfoStyle.Compact -> CompactMusicInfo(track, onMore, modifier)
        MusicInfoStyle.Big -> BigMusicInfo(track, onMore, modifier)
    }
}

@Composable
private fun CompactMusicInfo(
    track: Track,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // 歌名
            Text(
                text = track.title,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp, // max(2vh, 1em) ≈ 14sp
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 艺术家
            Text(
                text = track.artists.joinToString(", ") { it.name },
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 专辑名
            Text(
                text = track.album.title,
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // More 按钮
        IconButton(
            onClick = onMore,
            modifier = Modifier
                .size(28.dp) // 3.5vh ≈ 28dp
                .clip(CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "更多",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun BigMusicInfo(
    track: Track,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp) // 2em padding
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 歌名 - 大字号
                Text(
                    text = track.title,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 20.sp, // 较大字号
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 艺术家
                Text(
                    text = track.artists.joinToString(", ") { it.name },
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 专辑名
                Text(
                    text = track.album.title,
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // More 按钮
            IconButton(
                onClick = onMore,
                modifier = Modifier
                    .size(28.dp) // 3.5vh ≈ 28dp
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "更多",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
