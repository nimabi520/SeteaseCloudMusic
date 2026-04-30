package com.example.seteasecloudmusic.feature.player.presentation.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest

@Composable
fun LyricBackground(
    coverUrl: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dominantColor by remember(coverUrl) { mutableStateOf(Color.DarkGray) }
    var blurredBitmap by remember(coverUrl) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(coverUrl) {
        coverUrl?.let { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            runCatching {
                context.imageLoader.execute(request).drawable
            }.getOrNull()?.let { drawable ->
                val bmp = (drawable as? BitmapDrawable)?.bitmap
                bmp?.let {
                    Palette.from(it).generate { palette ->
                        palette?.dominantSwatch?.rgb?.let { rgb ->
                            dominantColor = Color(rgb)
                        }
                    }
                    // 低版本预渲染模糊 bitmap
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        blurredBitmap = blurBitmap(context, it, 20f)
                    }
                }
            }
        }
    }

    val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Box(modifier = modifier.fillMaxSize()) {
        if (canBlur) {
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(60.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.95f
            )
        } else {
            // 低版本：使用预模糊的 bitmap
            val bmp = blurredBitmap
            if (bmp != null) {
                androidx.compose.foundation.Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.95f
                )
            } else {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.9f
                )
            }
        }

        // 轻度的径向色彩叠加，增强歌词可读性但不形成半透明卡片感
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        dominantColor.copy(alpha = 0.18f),
                        dominantColor.copy(alpha = 0.08f),
                        Color.Black.copy(alpha = 0.6f)
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.35f),
                    radius = size.maxDimension * 0.8f
                ),
                size = size
            )
        }

        // 竖直渐变以保证底部控制区有足够对比
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )
    }
}

private fun blurBitmap(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
    val inputBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 4, bitmap.height / 4, false)
    val outputBitmap = Bitmap.createBitmap(inputBitmap)
    val rs = RenderScript.create(context)
    val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
    val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
    val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    script.setRadius(radius.coerceIn(1f, 25f))
    script.setInput(tmpIn)
    script.forEach(tmpOut)
    tmpOut.copyTo(outputBitmap)
    rs.destroy()
    return outputBitmap
}
