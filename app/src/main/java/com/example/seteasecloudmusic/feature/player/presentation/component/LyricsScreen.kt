package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.example.seteasecloudmusic.feature.player.domain.model.ParsedLyrics
import com.example.seteasecloudmusic.feature.player.presentation.LyricsUiState

@Composable
fun LyricsScreen(
    lyricsState: LyricsUiState,
    currentPosition: Int,
    activeLineIndex: Int,
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
                activeLineIndex = activeLineIndex
            )
        }
    }
}

@Composable
private fun LyricsContent(
    lyrics: ParsedLyrics,
    currentPosition: Int,
    activeLineIndex: Int
) {
    val listState = rememberLazyListState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current

    LaunchedEffect(activeLineIndex) {
        if (activeLineIndex >= 0) {
            val centerOffset = with(density) { -(screenHeight / 2).roundToPx() }
            listState.animateScrollToItem(
                index = activeLineIndex,
                scrollOffset = centerOffset
            )
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(
            vertical = max(240.dp, screenHeight / 2 - 80.dp),
            horizontal = 32.dp
        )
    ) {
        itemsIndexed(
            items = lyrics.lines,
            key = { _, line -> line.startTime }
        ) { index, line ->
            val distanceFromActive = kotlin.math.abs(index - activeLineIndex)
            val isActive = index == activeLineIndex

            val baseAlpha = when {
                isActive -> 1f
                distanceFromActive == 1 -> 0.35f
                distanceFromActive == 2 -> 0.18f
                else -> 0.08f
            }
            val blurRadius = when {
                isActive -> 0.dp
                distanceFromActive == 1 -> 2.dp
                distanceFromActive == 2 -> 6.dp
                else -> 12.dp
            }

            LyricLineItem(
                line = line,
                isActive = isActive,
                distanceFromActive = distanceFromActive,
                currentPosition = currentPosition,
                hasWordTiming = lyrics.hasWordTiming,
                baseAlpha = baseAlpha,
                blurRadius = blurRadius,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }
    }
}
