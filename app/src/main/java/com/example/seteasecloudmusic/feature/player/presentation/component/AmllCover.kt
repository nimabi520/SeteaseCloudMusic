package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kyant.shapes.RoundedRectangle

@Composable
fun AmllCover(
    coverUrl: String?,
    musicPaused: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val targetScale = if (musicPaused) 0.75f else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = if (musicPaused)
            tween(
                durationMillis = 600,
                easing = CubicBezierEasing(0.4f, 0.2f, 0.1f, 1f) // 暂停: 软收
            )
        else
            tween(
                durationMillis = 500,
                easing = CubicBezierEasing(0.3f, 0.2f, 0.2f, 1.4f) // 播放: 略带回弹
            ),
        label = "cover-scale"
    )
    val shadowAlpha by animateFloatAsState(
        targetValue = if (musicPaused) 0.12f else 0.19f,
        animationSpec = tween(500),
        label = "shadow-alpha"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = with(density) { 16.dp.toPx() }
                shape = RoundedRectangle(cornerRadius = 24.dp) // Squircle (连续曲率)
                ambientShadowColor = Color.Black.copy(alpha = shadowAlpha)
                spotShadowColor = Color.Black.copy(alpha = shadowAlpha)
                clip = true
            }
            .background(Color.Black)
    ) {
        AsyncImage(
            model = coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
