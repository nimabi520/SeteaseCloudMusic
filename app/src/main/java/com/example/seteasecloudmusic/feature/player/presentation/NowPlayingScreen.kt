package com.example.seteasecloudmusic.feature.player.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.seteasecloudmusic.core.model.Track
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
    val pagerState = rememberPagerState(pageCount = { 2 })
    val currentTrack = playbackState.currentTrack
    val isPlaying = playbackState.status == PlayerStatus.PLAYING
    val duration = playbackState.durationMs.coerceAtLeast(1)
    val position = playbackState.currentPositionMs.coerceIn(0, duration)

    Box(modifier = Modifier.fillMaxSize()) {
        // 最底层：共享的动态流体背景
        LyricBackground(coverUrl = currentTrack?.coverUrl)

        // 内容层：横向翻页（封面 / 歌词）
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                0 -> AlbumCoverPage(
                    track = currentTrack,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> LyricsScreen(
                    lyricsState = lyricsState,
                    currentPosition = currentPosition,
                    activeLineIndex = activeLineIndex,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 顶层：极简控制区
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 顶部：仅保留收起按钮 + 页指示器
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                PagerIndicator(pageCount = 2, currentPage = pagerState.currentPage)
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // 底部：大按钮、大间距、信息层级清晰
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
                    .navigationBarsPadding()
                    .padding(horizontal = 32.dp, vertical = 24.dp)
            )
        }
    }
}

@Composable
private fun AlbumCoverPage(
    track: Track?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = track?.coverUrl,
            contentDescription = track?.title,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(24.dp)
        )
        Text(
            text = track?.title ?: "未知歌曲",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color.White,
                textAlign = TextAlign.Center
            )
        )
        Text(
            text = track?.artists?.joinToString(" / ") { it.name } ?: "未知歌手",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
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

@Composable
private fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(pageCount) { index ->
            val alpha = if (index == currentPage) 0.8f else 0.3f
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = Color.White.copy(alpha = alpha),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
}

private fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
