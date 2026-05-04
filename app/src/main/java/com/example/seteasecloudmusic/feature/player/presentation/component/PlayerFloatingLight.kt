package com.example.seteasecloudmusic.feature.player.presentation.component

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.seteasecloudmusic.core.util.BitmapResolver
import com.flaviofaria.kenburnsview.KenBurnsView
import com.flaviofaria.kenburnsview.RandomTransitionGenerator
import com.google.android.renderscript.Toolkit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PlayerFloatingLight(
    coverUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var processedDrawable by remember(coverUrl) { mutableStateOf<Drawable?>(null) }

    // 异步处理封面图
    LaunchedEffect(coverUrl) {
        coverUrl?.let { url ->
            withContext(Dispatchers.IO) {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .build()
                val bitmap = (context.imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    val processed = imageResolve(bitmap)
                    processedDrawable = processed.toDrawable(context.resources)
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 底层：黑色兜底（最先声明 = 最底层）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )

        // 中层：Ken Burns 动效层（模糊封面缓动动画）
        KenBurnsBackgroundLayer(
            drawable = processedDrawable,
            isPlaying = isPlaying
        )

        // 顶层：半透明遮罩层（暗化 Ken Burns 效果）
        OverlayLayer(drawable = processedDrawable)
    }
}

@Composable
private fun KenBurnsBackgroundLayer(
    drawable: Drawable?,
    isPlaying: Boolean
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val isActive = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

    AndroidView(
        factory = { context ->
            KenBurnsView(context).apply {
                setTransitionGenerator(
                    RandomTransitionGenerator(
                        12000,  // 12秒周期
                        AccelerateDecelerateInterpolator()
                    )
                )
            }
        },
        update = { view ->
            drawable?.let {
                if (view.drawable != it) {
                    view.setImageDrawable(it)
                }
                if (isPlaying && isActive) {
                    view.resume()
                } else {
                    view.pause()
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun OverlayLayer(
    drawable: Drawable?
) {
    val alpha by animateFloatAsState(
        targetValue = 0.618f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "overlay-alpha"
    )

    AsyncImage(
        model = drawable,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
                this.alpha = alpha
            },
        colorFilter = ColorFilter.tint(
            Color(0x33000000),
            BlendMode.Overlay
        )
    )
}

// 图像处理函数 - 复刻 FlamingoSank 的 imageResolve
private fun imageResolve(image: Bitmap, moreLight: Boolean = false): Bitmap {
    var resizedBitmap = BitmapResolver.bitmapCompress(image, 96)
    resizedBitmap = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true)

    val canvas = Canvas(resizedBitmap)
    val paint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
        isDither = true
    }

    // 饱和度增强至 300%
    val saturationMatrix = ColorMatrix()
    saturationMatrix.setSaturation(3f)
    paint.colorFilter = ColorMatrixColorFilter(saturationMatrix)
    canvas.drawBitmap(resizedBitmap, 0f, 0f, paint)

    // 色调叠加
    if (moreLight) {
        canvas.drawColor(0x1AFFFFFF.toInt())
        canvas.drawColor(0xFFFFFFFF.toInt(), PorterDuff.Mode.OVERLAY)
        canvas.drawColor(0x52FFFFFF.toInt())
        canvas.drawColor(0xBFFFFFFF.toInt(), PorterDuff.Mode.OVERLAY)
    } else {
        canvas.drawColor(0x33000000.toInt(), PorterDuff.Mode.OVERLAY)
        canvas.drawColor(0x40000000.toInt())
    }

    // 高斯模糊
    resizedBitmap = Toolkit.blur(resizedBitmap, 25)
    return resizedBitmap
}
