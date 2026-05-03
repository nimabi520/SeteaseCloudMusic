package com.example.seteasecloudmusic.feature.player.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun AmllVerticalLayout(
    modifier: Modifier = Modifier,
    hideLyric: Boolean,
    thumbSlot: @Composable () -> Unit,
    smallControlsSlot: @Composable () -> Unit,
    lyricSlot: @Composable () -> Unit,
    bigControlsSlot: @Composable () -> Unit,
    coverSlot: @Composable () -> Unit
) {
    // 测量两个占位的位置
    var smallCoverBounds by remember { mutableStateOf<Rect?>(null) }
    var bigCoverBounds by remember { mutableStateOf<Rect?>(null) }

    // 动画状态
    val lyricAlpha by animateFloatAsState(
        targetValue = if (hideLyric) 0f else 1f,
        animationSpec = tween(500),
        label = "lyric-alpha"
    )
    val noLyricAlpha by animateFloatAsState(
        targetValue = if (hideLyric) 1f else 0f,
        animationSpec = tween(500),
        label = "no-lyric-alpha"
    )

    Box(modifier = modifier) {
        // ===== 4 行根网格 (用 Column + 固定高度 vs weight 模拟 grid-template-rows) =====
        Column(modifier = Modifier.fillMaxSize()) {
            // [drag-area] 30px
            Spacer(Modifier.height(30.dp))

            // [thumb] 30px
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                contentAlignment = Alignment.Center
            ) {
                thumbSlot()
            }

            // [main-view] 1fr — 用 Box 容纳两个重叠子网格
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                // ====== 子网格 1: lyricLayout ======
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = lyricAlpha },
                    horizontalAlignment = Alignment.Start
                ) {
                    // 顶部行: [3em margin] [phonySmallCover 6em] [smallControls 1fr] [3em margin]
                    Spacer(Modifier.height(12.dp)) // 1em top spacer

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 36.dp), // 3em margin
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // phonySmallCover (透明占位 6em ≈ 72.dp)
                        Spacer(
                            modifier = Modifier
                                .size(72.dp)
                                .onGloballyPositioned { coords ->
                                    smallCoverBounds = coords.boundsInParent()
                                }
                        )

                        Spacer(Modifier.width(12.dp)) // padding-left: 1em

                        Box(modifier = Modifier.weight(1f)) {
                            smallControlsSlot()
                        }
                    }

                    // 大歌词区 (剩余空间)
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        lyricSlot()
                    }

                    // [bottom-controls] 0fr
                    Spacer(Modifier.height(0.dp))
                }

                // ====== 子网格 2: noLyricLayout (与子网格1完全重叠) ======
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = noLyricAlpha
                        }
                ) {
                    // 顶部留白
                    Spacer(Modifier.height(12.dp))

                    // [cover-view 1fr]: 大封面占位
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 36.dp), // 3em margin
                        contentAlignment = Alignment.Center
                    ) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(1f)
                                .onGloballyPositioned { coords ->
                                    bigCoverBounds = coords.boundsInParent()
                                }
                        )
                    }

                    // [lyric-view 0fr]: bigControls (高度由内容决定)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 36.dp)
                    ) {
                        bigControlsSlot()
                    }
                }

                // ====== 真实封面 absolute + 动画 ======
                AmllCoverFrame(
                    smallBounds = smallCoverBounds,
                    bigBounds = bigCoverBounds,
                    hideLyric = hideLyric,
                    content = coverSlot
                )
            }
        }
    }
}
