package com.example.seteasecloudmusic.feature.player.presentation.component

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seteasecloudmusic.feature.player.domain.model.LyricLine

private val appleEasing = androidx.compose.animation.core.CubicBezierEasing(0.75f, 0.0f, 0.25f, 1.0f)

@Composable
fun LyricLineItem(
    line: LyricLine,
    isActive: Boolean,
    distanceFromActive: Int,
    currentPosition: Int,
    hasWordTiming: Boolean,
    showTranslation: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 缩放动画：当前行略微放大
    val tweenActive = remember {
        TweenSpec<Float>(durationMillis = 280, easing = appleEasing, delay = 80)
    }
    val tweenInactive = remember {
        TweenSpec<Float>(durationMillis = 280, easing = appleEasing, delay = 40)
    }
    val targetScale = when {
        line.isBG && isActive -> 0.95f
        line.isBG -> 0.92f
        isActive -> 1.01f
        else -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = if (isActive) tweenActive else tweenInactive,
        label = "lineScale"
    )

    // Alpha 动画：非当前行淡化
    val alphaTweenActive = remember {
        TweenSpec<Float>(durationMillis = 300, easing = appleEasing, delay = 100)
    }
    val alphaTweenInactive = remember {
        TweenSpec<Float>(durationMillis = 300, easing = appleEasing, delay = 60)
    }
    val targetAlpha = if (isActive) 1f else 0.14f
    val animatedAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = if (isActive) alphaTweenActive else alphaTweenInactive,
        label = "lineAlpha"
    )

    // 模糊效果 (Android 12+)
    val supportBlur = remember { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
    val targetBlurDp = remember(distanceFromActive, isActive, supportBlur) {
        derivedStateOf {
            when {
                !supportBlur || isActive || distanceFromActive == 0 -> 0f
                else -> (distanceFromActive * 1.5f).coerceAtMost(4f)
            }
        }
    }
    val blurDp by animateDpAsState(
        targetValue = targetBlurDp.value.dp,
        animationSpec = tween(durationMillis = 300),
        label = "lineBlur"
    )

    val horizontalAlign = if (line.isDuet) Alignment.End else Alignment.Start
    val transformOrigin = if (line.isDuet) TransformOrigin(1f, 0.5f) else TransformOrigin(0f, 0.5f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 9.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = animatedAlpha
                compositingStrategy = CompositingStrategy.ModulateAlpha
                this.transformOrigin = transformOrigin
            }
            .then(
                if (blurDp > 0.dp && supportBlur) {
                    Modifier.blur(blurDp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                } else {
                    Modifier
                }
            ),
        horizontalAlignment = horizontalAlign
    ) {
        AppleStyleLyricLine(
            line = line,
            isActive = isActive,
            currentPositionMs = currentPosition,
            hasWordTiming = hasWordTiming,
            isDuet = line.isDuet,
            onClick = onClick
        )

        // 翻译/注音文本
        val subText = line.translatedLyric.takeIf { !it.isNullOrBlank() }
            ?: line.romanLyric.takeIf { !it.isNullOrBlank() }

        AnimatedVisibility(
            visible = showTranslation && subText != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (subText != null) {
                val translationAlpha by animateFloatAsState(
                    targetValue = if (isActive) 0.5f else 0.14f,
                    animationSpec = if (isActive) alphaTweenActive else alphaTweenInactive,
                    label = "translationAlpha"
                )
                Text(
                    text = subText,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = translationAlpha),
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }
    }
}
