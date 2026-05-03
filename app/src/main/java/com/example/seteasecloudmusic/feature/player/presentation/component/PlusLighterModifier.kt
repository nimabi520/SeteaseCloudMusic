package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer

/**
 * 实现 AMLL 的 mix-blend-mode: plus-lighter 效果
 * 文字/控件与背景叠加时"加亮"，而非普通 alpha 混合
 *
 * 注意: 仅在 musicInfo / progressBar 标签 / 歌词区等关键文字上启用,
 * 不要在大封面或全屏背景上用, 否则离屏 layer 会拖性能。
 */
fun Modifier.plusLighter(): Modifier = this
    .graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
    }
    .drawWithContent {
        drawContent()
        // 强制以 Plus blend 合成回父 canvas
        drawIntoCanvas { canvas ->
            val paint = Paint().apply { blendMode = BlendMode.Plus }
            canvas.saveLayer(Rect(Offset.Zero, size), paint)
            canvas.restore()
        }
    }

/**
 * 实现 AMLL 的 mask-image 渐隐效果
 * 用于歌词区顶部 10% 透明渐变到完全不透明
 */
fun Modifier.topFadeMask(fadePercentage: Float = 0.1f): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                fadePercentage to Color.Black,
                1f to Color.Black
            ),
            blendMode = BlendMode.DstIn
        )
    }

/**
 * 实现 AMLL 的沉浸式封面底部渐隐效果
 * 封面底部 30% 区域渐变透明
 */
fun Modifier.bottomFadeMask(fadePercentage: Float = 0.3f): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Black,
                (1f - fadePercentage) to Color.Black,
                1f to Color.Transparent
            ),
            blendMode = BlendMode.DstIn
        )
    }
