package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.animation.core.LinearEasing
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
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun InterludeDots(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "interlude")
    val phase = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val intro = ((progress - 0.06f) / 0.2f).coerceIn(0f, 1f)
    val outro = ((1f - progress) / 0.22f).coerceIn(0f, 1f)
    val globalAlpha = intro * outro
    val breathScale = 0.92f + 0.08f * (sin((phase.value + 0.25f) * 2f * PI).toFloat() * 0.5f + 0.5f)

    Row(
        modifier = modifier.graphicsLayer {
            alpha = globalAlpha
            scaleX = breathScale
            scaleY = breathScale
        },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            val shiftedPhase = (phase.value + index * 0.17f) % 1f
            val dotAlpha = 0.25f + 0.75f * (sin(shiftedPhase * 2f * PI).toFloat() * 0.5f + 0.5f)
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer {
                        alpha = dotAlpha
                    }
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    )
            )
        }
    }
}
