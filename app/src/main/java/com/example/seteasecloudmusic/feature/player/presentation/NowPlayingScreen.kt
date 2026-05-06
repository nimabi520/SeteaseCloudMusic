package com.example.seteasecloudmusic.feature.player.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.example.seteasecloudmusic.core.player.PlaybackState
import com.example.seteasecloudmusic.core.player.PlayerStatus
import com.example.seteasecloudmusic.core.util.BitmapResolver
import kotlin.math.roundToInt

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
    val context = LocalContext.current
    val track = playbackState.currentTrack
    val coverUrl = track?.coverUrl ?: track?.album?.coverUrl
    val isPlaying = playbackState.status == PlayerStatus.PLAYING

    // 颜色采样状态
    var dominantColor by remember { mutableStateOf(Color(0xFF1A1A2E)) }

    // 拖拽偏移量（用于滑动退出）
    var offsetY by remember { mutableFloatStateOf(0f) }

    // 从专辑封面提取主题色
    LaunchedEffect(coverUrl) {
        if (coverUrl.isNullOrBlank()) {
            dominantColor = Color(0xFF1A1A2E)
            return@LaunchedEffect
        }

        try {
            val request = ImageRequest.Builder(context)
                .data(coverUrl)
                .allowHardware(false)
                .size(96)
                .build()

            val imageLoader = ImageLoader(context)
            val result = imageLoader.execute(request)
            val drawable = (result as? SuccessResult)?.drawable

            if (drawable != null) {
                val bitmap = drawable.toBitmap()
                val compressed = BitmapResolver.bitmapCompress(bitmap, 96)
                val palette = Palette.from(compressed).generate()
                val swatch = palette.darkVibrantSwatch
                    ?: palette.darkMutedSwatch
                    ?: palette.vibrantSwatch
                    ?: palette.dominantSwatch

                if (swatch != null) {
                    dominantColor = Color(swatch.rgb)
                }
            }
        } catch (_: Exception) {
            // 保持默认颜色
        }
    }

    BackHandler(onBack = onClose)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, offsetY.toInt()) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (offsetY > 200.dp.toPx()) {
                            onClose()
                        } else {
                            offsetY = 0f
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount > 0f || offsetY > 0f) {
                            offsetY = (offsetY + dragAmount).coerceAtLeast(0f)
                        }
                    }
                )
            }
    ) {
        // ── Layer 0: 专辑封面 ──
        if (coverUrl != null) {
            AsyncImage(
                model = coverUrl,
                contentDescription = "专辑封面",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .align(Alignment.TopCenter)
            )
        } else {
            // 无封面时的占位背景
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(Color.DarkGray)
                    .align(Alignment.TopCenter)
            )
        }

        // ── Layer 1: 顶部状态栏渐变遮罩 ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
                .align(Alignment.TopCenter)
        )

        // ── Layer 2: 底部渐变过渡 ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            dominantColor.copy(alpha = 0.7f),
                            dominantColor
                        )
                    )
                )
                .align(Alignment.BottomCenter)
        )

        // ── Layer 3: 内容层 ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // 顶部拖动手柄 + 返回按钮
            Column(modifier = Modifier.fillMaxWidth()) {
                // 拖动手柄（小白条）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .background(
                                Color.White.copy(alpha = 0.6f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }

                // 返回按钮
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExpandMore,
                            contentDescription = "关闭",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Spacer 把歌曲信息推到封面下方
            Spacer(modifier = Modifier.fillMaxHeight(0.28f))

            // 歌曲信息 + 喜欢按钮
            if (track != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧：歌曲信息
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = track.artists.joinToString(" / ") { it.name }.ifBlank { "未知艺术家" },
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // 右侧：喜欢按钮
                    IconButton(
                        onClick = { /* TODO: 喜欢功能 */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FavoriteBorder,
                            contentDescription = "喜欢",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // 歌词区域
            when (lyricsState) {
                is LyricsUiState.Success -> {
                    LyricsColumn(
                        lyrics = lyricsState.lyrics,
                        activeLineIndex = activeLineIndex,
                        currentTimeMs = currentPosition,
                        modifier = Modifier.weight(1f)
                    )
                }
                is LyricsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无歌词",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // 底部控制区
            PlayerControls(
                currentPositionMs = currentPosition,
                durationMs = playbackState.durationMs,
                isPlaying = isPlaying,
                dominantColor = dominantColor,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
                onSeekTo = onSeekTo
            )
        }
    }
}
