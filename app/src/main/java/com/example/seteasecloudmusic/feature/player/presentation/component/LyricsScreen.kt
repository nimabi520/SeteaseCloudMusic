package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import com.example.seteasecloudmusic.feature.player.domain.model.ParsedLyrics
import com.example.seteasecloudmusic.feature.player.presentation.LyricsUiState

@Composable
fun LyricsScreen(
    lyricsState: LyricsUiState,
    currentPosition: Int,
    activeLineIndex: Int,
    isPlaying: Boolean,
    onLineClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (val state = lyricsState) {
            is LyricsUiState.Idle,
            is LyricsUiState.Loading -> Unit
            is LyricsUiState.Error -> Unit
            is LyricsUiState.Success -> LyricsContent(
                lyrics = state.lyrics,
                currentPosition = currentPosition,
                activeLineIndex = activeLineIndex,
                isPlaying = isPlaying,
                onLineClick = onLineClick
            )
        }
    }
}

@Composable
private fun LyricsContent(
    lyrics: ParsedLyrics,
    currentPosition: Int,
    activeLineIndex: Int,
    isPlaying: Boolean,
    onLineClick: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current
    var isAutoScrolling by remember { mutableStateOf(false) }
    var userFollowLocked by remember { mutableStateOf(false) }
    var userFollowUnlockAt by remember { mutableLongStateOf(0L) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling ->
                if (scrolling && !isAutoScrolling) {
                    userFollowLocked = true
                    userFollowUnlockAt = System.currentTimeMillis() + 2200L
                }
            }
    }

    LaunchedEffect(userFollowLocked, activeLineIndex, isPlaying) {
        if (userFollowLocked && isPlaying) {
            val waitMs = (userFollowUnlockAt - System.currentTimeMillis()).coerceAtLeast(0L)
            delay(waitMs)
            userFollowLocked = false
        }
    }

    LaunchedEffect(activeLineIndex, userFollowLocked, isPlaying) {
        if (activeLineIndex >= 0 && !userFollowLocked && isPlaying) {
            // 将当前行定位到视口顶部往下约 15% 屏幕高度处（整体约 25% 屏高）
            val scrollOffset = with(density) { (screenHeight * 0.15f).roundToPx() }
            isAutoScrolling = true
            runCatching {
                listState.animateScrollToItem(
                    index = activeLineIndex,
                    scrollOffset = scrollOffset
                )
            }
            isAutoScrolling = false
        }
    }

    val interludeProgress = remember(currentPosition, lyrics.lines) {
        resolveInterludeProgress(currentPosition, lyrics.lines)
    }

    Box(modifier = Modifier.fillMaxSize().padding(top = 76.dp, bottom = 300.dp)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) {
            itemsIndexed(
                items = lyrics.lines,
                key = { _, line -> line.startTime }
            ) { index, line ->
                val distanceFromActive = kotlin.math.abs(index - activeLineIndex)
                val isActive = index == activeLineIndex

                val baseAlpha = when {
                    isActive -> 1f
                    distanceFromActive == 1 -> 0.42f
                    distanceFromActive == 2 -> 0.2f
                    else -> 0.1f
                }
                val blurRadius = when {
                    isActive -> 0.dp
                    distanceFromActive == 1 -> 1.5.dp
                    distanceFromActive == 2 -> 4.dp
                    else -> 8.dp
                }

                LyricLineItem(
                    line = line,
                    isActive = isActive,
                    distanceFromActive = distanceFromActive,
                    currentPosition = currentPosition,
                    hasWordTiming = lyrics.hasWordTiming,
                    baseAlpha = baseAlpha,
                    blurRadius = blurRadius,
                    onClick = {
                        userFollowLocked = false
                        onLineClick(line.startTime)
                    },
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }

        if (interludeProgress != null) {
            InterludeDots(
                progress = interludeProgress,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

private fun resolveInterludeProgress(
    currentPosition: Int,
    listState: List<com.example.seteasecloudmusic.feature.player.domain.model.LyricLine>
): Float? {
    if (listState.size < 2) return null
    for (i in 0 until listState.lastIndex) {
        val cur = listState[i]
        val next = listState[i + 1]
        val gap = next.startTime - cur.endTime
        if (gap < 2500) continue
        if (currentPosition in cur.endTime..next.startTime) {
            return ((currentPosition - cur.endTime).toFloat() / gap.toFloat()).coerceIn(0f, 1f)
        }
    }
    return null
}
