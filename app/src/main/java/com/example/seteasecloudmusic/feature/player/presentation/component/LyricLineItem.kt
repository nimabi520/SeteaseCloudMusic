package com.example.seteasecloudmusic.feature.player.presentation.component

import android.os.Build
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.seteasecloudmusic.feature.player.domain.model.LyricLine

@Composable
fun LyricLineItem(
    line: LyricLine,
    isActive: Boolean,
    distanceFromActive: Int,
    currentPosition: Int,
    hasWordTiming: Boolean,
    baseAlpha: Float,
    blurRadius: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val springSpec = spring<Float>(
        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
        stiffness = if (isActive) 110f else 80f
    )

    val targetScale = when {
        line.isBG && isActive -> 0.78f
        line.isBG -> 0.72f
        isActive -> 1.03f
        else -> 1f
    }
    val scale by animateFloatAsState(targetScale, animationSpec = springSpec, label = "scale")

    val horizontalAlign = when {
        line.isDuet -> Alignment.End
        else -> Alignment.Start
    }
    val sidePadding = if (line.isDuet) 52.dp else 22.dp
    val farLineAlphaPenalty = when {
        distanceFromActive == 0 -> 1f
        distanceFromActive == 1 -> 0.96f
        distanceFromActive == 2 -> 0.9f
        else -> 0.82f
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (line.isDuet) 64.dp else sidePadding,
                end = if (line.isDuet) sidePadding else 64.dp
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.alpha = baseAlpha * farLineAlphaPenalty
                if (blurRadius > 0.dp && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    this.renderEffect = RenderEffect.createBlurEffect(
                        blurRadius.value, blurRadius.value,
                        Shader.TileMode.DECAL
                    ).asComposeRenderEffect()
                }
                if (line.isDuet) transformOrigin = TransformOrigin(1f, 0.5f)
            },
        horizontalAlignment = horizontalAlign
    ) {
        val mainText = line.words.joinToString("") { it.word }

        if (hasWordTiming && isActive) {
            WordByWordLyric(
                words = line.words,
                currentPositionMs = currentPosition,
                isBgLine = line.isBG
            )
        } else {
            Text(
                text = mainText,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = if (line.isBG) 19.sp else 33.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isActive) Color.White else Color.White.copy(alpha = 0.52f)
                )
            )
        }

        val subText = line.translatedLyric.takeIf { it.isNotBlank() }
            ?: line.romanLyric.takeIf { it.isNotBlank() }
        if (subText != null) {
            Text(
                text = subText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 1.5.em
                ),
                color = Color.White.copy(alpha = if (isActive) 0.36f else 0.24f),
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
