package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.tanh

@Composable
fun AmllBouncingSlider(
    value: Float,
    max: Float,
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    beforeIcon: @Composable (() -> Unit)? = null,
    afterIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 三个状态(对应 progressMv, insetMv, bounceXSpring)
    val progressAnim = remember { Animatable(value / max.coerceAtLeast(0.001f)) }
    val insetAnim = remember { Animatable(6f) }    // dp; 0=展开, 6=收起
    val bounceAnim = remember { Animatable(0f) }   // dp; 横向偏移
    var isDragging by remember { mutableStateOf(false) }

    // 进度跟随外部 value 更新(非拖拽时)
    LaunchedEffect(value, isDragging) {
        if (!isDragging) {
            progressAnim.animateTo(value / max.coerceAtLeast(0.001f))
        }
    }

    // 播放时本地推进
    LaunchedEffect(isPlaying, max) {
        var lastFrame = withFrameNanos { it }
        while (isPlaying && !isDragging) {
            val now = withFrameNanos { it }
            val deltaMs = (now - lastFrame) / 1_000_000f
            lastFrame = now
            val current = progressAnim.value * max + deltaMs
            progressAnim.snapTo((current / max).coerceIn(0f, 1f))
        }
    }

    Row(
        modifier = modifier
            .graphicsLayer {
                translationX = with(density) { bounceAnim.value.dp.toPx() }
            }
            .pointerInput(max) {
                detectTapGestures { offset ->
                    val widthPx = size.width.toFloat()
                    val newProgress = (offset.x / widthPx).coerceIn(0f, 1f)
                    scope.launch {
                        progressAnim.snapTo(newProgress)
                    }
                    onSeek(newProgress * max)
                }
            }
            .pointerInput(max) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        scope.launch {
                            insetAnim.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(280, easing = EaseOut)
                            )
                        }
                        val widthPx = size.width.toFloat()
                        scope.launch {
                            progressAnim.snapTo((offset.x / widthPx).coerceIn(0f, 1f))
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val widthPx = size.width.toFloat()
                        val rel = (progressAnim.value * widthPx + dragAmount.x) / widthPx
                        val bounce = when {
                            rel < 0f -> tanh(rel * 2f) * 12f
                            rel > 1f -> tanh((rel - 1f) * 2f) * 12f
                            else -> 0f
                        }
                        scope.launch { bounceAnim.snapTo(bounce) }
                        scope.launch {
                            progressAnim.snapTo(rel.coerceIn(0f, 1f))
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeek(progressAnim.value * max)
                        scope.launch {
                            insetAnim.animateTo(
                                targetValue = 6f,
                                animationSpec = spring(0.55f, 200f)
                            )
                        }
                        scope.launch {
                            bounceAnim.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(0.4f, 300f)
                            )
                        }
                    }
                )
            }
            .fillMaxWidth()
            .height(24.dp), // 总高度(含 padding 区域)
        verticalAlignment = Alignment.CenterVertically
    ) {
        beforeIcon?.invoke()

        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .padding(vertical = with(density) { insetAnim.value.dp }) // 0=20dp 全展, 6dp=8dp 紧凑
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressAnim.value.coerceIn(0f, 1f))
                    .height(20.dp)
                    .background(Color.White.copy(alpha = 0.4f))
            )
        }

        afterIcon?.invoke()
    }
}
