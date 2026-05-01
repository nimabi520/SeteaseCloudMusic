package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.RoundedRectangle

@Composable
fun GlassProgressBar(
    progress: Float,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val trackHeight = 8.dp
    val shape = { RoundedRectangle(trackHeight / 2) }

    Box(modifier = modifier.height(trackHeight)) {
        // Track: semi-transparent glass
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = shape,
                    effects = {
                        vibrancy()
                        blur(2.4f.dp.toPx())
                        lens(10f.dp.toPx(), 18f.dp.toPx())
                    },
                    onDrawSurface = { drawRect(Color.White.copy(alpha = 0.22f)) }
                )
        )
        // Fill: opaque white
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = shape,
                    effects = {
                        vibrancy()
                        blur(1.2f.dp.toPx())
                    },
                    onDrawSurface = { drawRect(Color.White.copy(alpha = 0.96f)) }
                )
        )
    }
}
