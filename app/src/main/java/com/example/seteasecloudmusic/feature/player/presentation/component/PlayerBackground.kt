package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PlayerBackground(
    coverUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        PlayerFloatingLight(
            coverUrl = coverUrl,
            isPlaying = isPlaying
        )
    }
}
