package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

private val appleEasing = CubicBezierEasing(0.75f, 0.0f, 0.25f, 1.0f)

@Composable
fun InterludeDots(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "interlude")

    // 呼吸缩放动画
    val breathScale = transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = appleEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    Row(
        modifier = modifier.graphicsLayer {
            scaleX = breathScale.value
            scaleY = breathScale.value
            alpha = 0.8f
        },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 1..3) {
            val average = 1f / 3f
            val beforePadding = (i - 1) * average
            val thisPercent = (progress - beforePadding) / ((i * average) - beforePadding)
            val dotAlpha = 0.2f + (0.8f * thisPercent).coerceIn(0f, 0.8f)

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .graphicsLayer { alpha = dotAlpha }
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    )
            )
        }
    }
}
