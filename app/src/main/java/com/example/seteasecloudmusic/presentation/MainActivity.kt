package com.example.seteasecloudmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.seteasecloudmusic.presentation.navigation.AppNavigation

/**
 * 应用主入口 Activity，负责初始化 Compose 导航容器。
 */
class MainActivity : ComponentActivity() {
    /**
     * 创建页面并挂载应用导航。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground()
                AppNavigation()
            }
        }
    }
}

@Composable
private fun LiquidGlassBackground(modifier: Modifier = Modifier) {
    val tileSize = with(LocalDensity.current) { 160.dp.toPx() }
    val tileGap = with(LocalDensity.current) { 22.dp.toPx() }
    val cornerRadius = with(LocalDensity.current) { 24.dp.toPx() }
    val topPadding = with(LocalDensity.current) { 130.dp.toPx() }
    val sidePadding = with(LocalDensity.current) { 30.dp.toPx() }

    val palette = listOf(
        Color(0xFFE9425D),
        Color(0xFFE9923F),
        Color(0xFF65BE66),
        Color(0xFF5AB4C0)
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = Color(0xFFE6E6E6))

        val step = tileSize + tileGap
        val rowCount = ((size.height - topPadding) / step).toInt() + 2

        for (row in 0 until rowCount) {
            val y = topPadding + row * step
            for (column in 0 until palette.size) {
                val x = sidePadding + column * step
                drawRoundRect(
                    color = palette[column],
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(tileSize, tileSize),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            }
        }
    }
}