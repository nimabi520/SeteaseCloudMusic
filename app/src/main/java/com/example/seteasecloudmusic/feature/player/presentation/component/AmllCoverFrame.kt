package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun AmllCoverFrame(
    smallBounds: Rect?,
    bigBounds: Rect?,
    hideLyric: Boolean,
    content: @Composable () -> Unit
) {
    val target = if (hideLyric) bigBounds else smallBounds
    if (target == null) return // 等待第一次 measure

    val density = LocalDensity.current
    val springSpec = spring<Float>(
        stiffness = 200f,
        dampingRatio = 0.7f // 复刻 stiffness:200, damping:30
    )

    // 维持 1:1 (取宽高较小者作为最终边长)
    val sideTarget = minOf(target.width, target.height)
    val leftTarget = target.left + (target.width - sideTarget) / 2
    val topTarget = target.top + (target.height - sideTarget) / 2

    val left by animateFloatAsState(
        targetValue = leftTarget,
        animationSpec = springSpec,
        label = "cover-left"
    )
    val top by animateFloatAsState(
        targetValue = topTarget,
        animationSpec = springSpec,
        label = "cover-top"
    )
    val side by animateFloatAsState(
        targetValue = sideTarget,
        animationSpec = springSpec,
        label = "cover-side"
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(left.roundToInt(), top.roundToInt()) }
            .size(
                width = with(density) { side.toDp() },
                height = with(density) { side.toDp() }
            )
    ) {
        content()
    }
}
