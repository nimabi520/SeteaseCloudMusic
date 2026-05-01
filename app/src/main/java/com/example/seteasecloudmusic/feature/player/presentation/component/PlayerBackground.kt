package com.example.seteasecloudmusic.feature.player.presentation.component

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import androidx.palette.graphics.Palette

@Composable
fun PlayerBackground(
    coverUrl: String?,
    isLyricsPage: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dominantColor by remember(coverUrl) { mutableStateOf(Color(0xFF1A1A1A)) }

    LaunchedEffect(coverUrl) {
        coverUrl?.let { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            runCatching {
                context.imageLoader.execute(request).drawable
            }.getOrNull()?.let { drawable ->
                (drawable as? BitmapDrawable)?.bitmap?.let { bmp ->
                    Palette.from(bmp).generate { palette ->
                        palette?.dominantSwatch?.hsl?.let { hsl ->
                            dominantColor = Color.hsl(hsl[0], 0.12f, 0.08f)
                        }
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (coverUrl != null) {
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isLyricsPage) Modifier.blur(56.dp) else Modifier),
                contentScale = ContentScale.Crop
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            dominantColor.copy(alpha = if (isLyricsPage) 0.14f else 0.08f),
                            Color.Black.copy(alpha = 0.12f),
                            Color.Black.copy(alpha = if (isLyricsPage) 0.62f else 0.42f)
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.35f),
                        radius = size.maxDimension * 0.95f
                    ),
                    size = size
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.08f),
                            0.45f to Color.Transparent,
                            0.8f to Color.Black.copy(alpha = if (isLyricsPage) 0.46f else 0.28f),
                            1.0f to Color.Black.copy(alpha = if (isLyricsPage) 0.72f else 0.56f)
                        )
                    )
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(dominantColor))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.08f),
                            0.5f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.7f)
                        )
                    )
            )
        }
    }
}
