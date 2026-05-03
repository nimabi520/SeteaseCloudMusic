package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seteasecloudmusic.feature.player.domain.model.LyricLine
import com.example.seteasecloudmusic.feature.player.domain.model.LyricWord

private val appleEasing: Easing = EaseInOutQuad

private val FocusedColor = Color(0xFFFFFFFF)
private val UnfocusedColor = Color(0x2EFFFFFF)

private val ActiveTextStyle = TextStyle(
    fontSize = 36.sp,
    lineHeight = 46.sp,
    fontWeight = FontWeight.ExtraBold,
    letterSpacing = 0.05.sp,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

private val InactiveTextStyle = TextStyle(
    fontSize = 22.sp,
    lineHeight = 30.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.05.sp,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

private val BgTextStyle = TextStyle(
    fontSize = 16.sp,
    lineHeight = 22.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.05.sp,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

@Composable
fun AppleStyleLyricLine(
    line: LyricLine,
    isActive: Boolean,
    currentPositionMs: Int,
    hasWordTiming: Boolean,
    isDuet: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewAlign = if (isDuet) Alignment.End else Alignment.Start
    val textAlign = if (isDuet) TextAlign.End else TextAlign.Start
    val measurer = rememberTextMeasurer(cacheSize = 32)

    val style = when {
        line.isBG -> BgTextStyle
        isActive -> ActiveTextStyle
        else -> InactiveTextStyle
    }.let { if (isDuet) it.copy(textAlign = textAlign) else it }

    val mainText = remember(line.words) {
        line.words.joinToString("") { it.word }
    }

    val words = line.words

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.ModulateAlpha
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = viewAlign
    ) {
        SubcomposeLayout { subConstraints ->
            val measureResult = measurer.measure(
                text = mainText,
                style = style,
                constraints = Constraints(
                    minWidth = 0,
                    maxWidth = subConstraints.maxWidth
                ),
                layoutDirection = LayoutDirection.Ltr
            )

            val height = (style.lineHeight.toPx() * measureResult.lineCount).toInt()
            val width = runCatching {
                (0 until measureResult.lineCount).maxOf {
                    measureResult.getBoundingBox(
                        measureResult.getLineEnd(it, visibleEnd = true) - 1
                    ).right
                }
            }.getOrDefault(subConstraints.maxWidth.toFloat()).toInt()

            val content = subcompose(mainText) {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .drawWithCache {
                            onDrawBehind {
                                drawLyricWords(
                                    words = words,
                                    mainText = mainText,
                                    measureResult = measureResult,
                                    hasWordTiming = hasWordTiming,
                                    isActive = isActive,
                                    currentPositionMs = currentPositionMs,
                                    style = style,
                                    measurer = measurer
                                )
                            }
                        }
                )
            }.first()

            val placeable = content.measure(Constraints.fixed(width, height))

            layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
            }
        }
    }
}

private fun DrawScope.drawLyricWords(
    words: List<LyricWord>,
    mainText: String,
    measureResult: TextLayoutResult,
    hasWordTiming: Boolean,
    isActive: Boolean,
    currentPositionMs: Int,
    style: TextStyle,
    measurer: TextMeasurer
) {
    if (!hasWordTiming || !isActive) {
        val color = if (isActive) FocusedColor else UnfocusedColor
        drawText(textLayoutResult = measureResult, color = color)
        return
    }

    var charSum = 0
    var lastTime = words.firstOrNull()?.startTime?.toFloat() ?: 0f
    val topOffsetDp = 2.dp

    words.forEachIndexed { wordIndex, word ->
        val thisWord = word.word
        if (thisWord.isEmpty()) return@forEachIndexed

        val wordStartTime = word.startTime.toFloat()
        val wordEndTime = word.endTime.toFloat()
        val averageTime = if (thisWord.length > 0) {
            (wordEndTime - lastTime) / thisWord.length
        } else 0f

        val prevTime = if (wordIndex - 1 < 0) {
            words.first().startTime.toFloat()
        } else {
            words[wordIndex - 1].startTime.toFloat()
        }

        val groupPercent = if ((wordStartTime - prevTime) == 0f) 0f
        else {
            ((currentPositionMs.toFloat() - prevTime) /
                    (wordStartTime - prevTime)).coerceIn(0f, 1f)
        }
        val easedPercent = appleEasing.transform(groupPercent.coerceIn(0f, 1f))
        val topOffsetPx = topOffsetDp.toPx() * easedPercent

        thisWord.forEach { char ->
            val charWord = char.toString()

            val thisCharLastTime = lastTime
            val thisCharAvgTime = averageTime

            val charIndex = charSum.coerceAtMost(mainText.length - 1).coerceAtLeast(0)
            val topLeft = measureResult.getBoundingBox(charIndex)
                .topLeft.minus(Offset(0f, topOffsetPx))

            val percent = if (charWord.isBlank() || thisCharAvgTime == 0f) 0f
            else ((currentPositionMs.toFloat() - thisCharLastTime) / thisCharAvgTime)

            val brush = if (charWord.isBlank()) {
                SolidColor(UnfocusedColor)
            } else {
                val px = 0.3f
                val beforeColor = if (percent <= -0.5f) UnfocusedColor else FocusedColor
                val afterColor = if (percent >= 1f) FocusedColor else UnfocusedColor

                Brush.horizontalGradient(
                    0f to beforeColor,
                    (percent - px).coerceIn(0f, 1f) to beforeColor,
                    (percent + px).coerceIn(0f, 1f) to afterColor
                )
            }

            val charLayout = measurer.measure(
                text = charWord,
                style = style,
                constraints = measureResult.layoutInput.constraints
            )

            drawText(
                textLayoutResult = charLayout,
                topLeft = topLeft,
                brush = brush
            )

            charSum++
            lastTime += averageTime
        }
    }
}
