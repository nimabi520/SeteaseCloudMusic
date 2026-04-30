package com.example.seteasecloudmusic.feature.player.presentation.component

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
        if (isLyricsPage) {
            Box(modifier = Modifier.fillMaxSize().background(dominantColor))
        } else {
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.5f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.5f)
                        )
                    )
            )
        }
    }
}
