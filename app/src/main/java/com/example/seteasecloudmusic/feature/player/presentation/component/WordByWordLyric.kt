package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.sp
import com.example.seteasecloudmusic.feature.player.domain.model.LyricWord
import kotlin.math.pow

@Deprecated("Replaced by AppleStyleLyricLine", ReplaceWith("AppleStyleLyricLine"))
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordByWordLyric(
    words: List<LyricWord>,
    currentPositionMs: Int,
    isBgLine: Boolean,
    modifier: Modifier = Modifier
) {
    val baseTextSize = if (isBgLine) 18.sp else 40.sp

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        words.forEach { word ->
            val duration = word.endTime - word.startTime
            val progress = calculateWordProgress(word, currentPositionMs)

            val brightness = when {
                progress <= 0f -> 0.28f
                progress >= 1f -> 1.0f
                else -> 0.28f + 0.72f * easeOutCubic(progress)
            }

            val isEmphasized = duration > 800 && progress in 0.05f..0.95f
            val emphasizeScale = if (isEmphasized) 1.06f else 1f
            val glowAlpha = if (isEmphasized && progress < 1f) 0.55f * progress else 0f

            val fontWeight = when {
                progress >= 1f -> FontWeight.SemiBold
                progress > 0.5f -> FontWeight.Medium
                else -> FontWeight.Normal
            }

            val animatedScale by animateFloatAsState(
                targetValue = emphasizeScale,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
                label = "wordScale"
            )

            Text(
                text = word.word,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = baseTextSize,
                    fontWeight = fontWeight,
                    color = Color.White.copy(alpha = brightness),
                    shadow = if (glowAlpha > 0.01f) Shadow(
                        color = Color.White.copy(alpha = glowAlpha),
                        blurRadius = 18f,
                        offset = Offset(0f, 0f)
                    ) else null
                ),
                modifier = Modifier.graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
            )
        }
    }
}

private fun calculateWordProgress(word: LyricWord, currentMs: Int): Float = when {
    currentMs <= word.startTime -> 0f
    currentMs >= word.endTime -> 1f
    else -> (currentMs - word.startTime).toFloat() / (word.endTime - word.startTime)
}

private fun easeOutCubic(x: Float): Float {
    return 1f - (1f - x).pow(3)
}
