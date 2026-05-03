package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import com.example.seteasecloudmusic.feature.player.domain.model.ParsedLyrics
import com.example.seteasecloudmusic.feature.player.presentation.LyricsUiState

private val scrollEasing = CubicBezierEasing(0.75f, 0.0f, 0.25f, 1.0f)

@Composable
fun LyricsScreen(
    lyricsState: LyricsUiState,
    currentPosition: Int,
    activeLineIndex: Int,
    isPlaying: Boolean,
    showTranslation: Boolean,
    onLineClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (val state = lyricsState) {
        is LyricsUiState.Idle,
        is LyricsUiState.Loading -> Unit
        is LyricsUiState.Error -> Unit
        is LyricsUiState.Success -> LyricsContent(
            lyrics = state.lyrics,
            currentPosition = currentPosition,
            activeLineIndex = activeLineIndex,
            isPlaying = isPlaying,
            showTranslation = showTranslation,
            onLineClick = onLineClick,
            modifier = modifier
        )
    }
}

@Composable
private fun LyricsContent(
    lyrics: ParsedLyrics,
    currentPosition: Int,
    activeLineIndex: Int,
    isPlaying: Boolean,
    showTranslation: Boolean,
    onLineClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current
    var isAutoScrolling by remember { mutableStateOf(false) }
    var userFollowLocked by remember { mutableStateOf(false) }
    var userFollowUnlockAt by remember { mutableLongStateOf(0L) }

    // 检测用户手动滚动
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling ->
                if (scrolling && !isAutoScrolling) {
                    userFollowLocked = true
                    userFollowUnlockAt = System.currentTimeMillis() + 1800L
                }
            }
    }

    // 用户滚动锁定超时恢复
    LaunchedEffect(userFollowLocked, activeLineIndex, isPlaying) {
        if (userFollowLocked && isPlaying) {
            val waitMs = (userFollowUnlockAt - System.currentTimeMillis()).coerceAtLeast(0L)
            delay(waitMs)
            userFollowLocked = false
        }
    }

    // 自动滚动跟随当前行
    LaunchedEffect(activeLineIndex, userFollowLocked, isPlaying) {
        if (activeLineIndex >= 0 && !userFollowLocked && isPlaying) {
            val targetOffset = with(density) { (screenHeight * 0.10f).roundToPx() }
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val targetItem = visibleItems.find { it.index == activeLineIndex + 1 }

            isAutoScrolling = true
            runCatching {
                if (targetItem != null) {
                    // 使用 animateScrollBy 实现平滑的 CubicBezierEasing 滚动
                    val scrollDistance = targetItem.offset - targetOffset
                    listState.animateScrollBy(
                        scrollDistance.toFloat(),
                        animationSpec = tween(durationMillis = 500, easing = scrollEasing)
                    )
                } else {
                    // 降级方案：目标项不在可见范围时使用 scrollToItem
                    listState.animateScrollToItem(
                        index = activeLineIndex,
                        scrollOffset = targetOffset
                    )
                }
            }
            isAutoScrolling = false
        }
    }

    val interludeProgress = remember(currentPosition, lyrics.lines) {
        resolveInterludeProgress(currentPosition, lyrics.lines)
    }

    // 歌词列表 + 垂直渐变遮罩
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(top = 76.dp, bottom = 300.dp)
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.06f to Color.Black,
                            0.92f to Color.Black,
                            1f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        itemsIndexed(
            items = lyrics.lines,
            key = { _, line -> line.startTime }
        ) { index, line ->
            val isActive = index == activeLineIndex
            val distanceFromActive = kotlin.math.abs(index - activeLineIndex)

            LyricLineItem(
                line = line,
                isActive = isActive,
                distanceFromActive = distanceFromActive,
                currentPosition = currentPosition,
                hasWordTiming = lyrics.hasWordTiming,
                showTranslation = showTranslation,
                onClick = {
                    userFollowLocked = false
                    onLineClick(line.startTime)
                },
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }

    // 间奏进度点
    if (interludeProgress != null) {
        InterludeDots(
            progress = interludeProgress,
            modifier = Modifier.padding(top = 120.dp)
        )
    }
}

private fun resolveInterludeProgress(
    currentPosition: Int,
    lines: List<com.example.seteasecloudmusic.feature.player.domain.model.LyricLine>
): Float? {
    if (lines.size < 2) return null
    for (i in 0 until lines.lastIndex) {
        val cur = lines[i]
        val next = lines[i + 1]
        val gap = next.startTime - cur.endTime
        if (gap < 2500) continue
        if (currentPosition in cur.endTime..next.startTime) {
            return ((currentPosition - cur.endTime).toFloat() / gap.toFloat()).coerceIn(0f, 1f)
        }
    }
    return null
}
