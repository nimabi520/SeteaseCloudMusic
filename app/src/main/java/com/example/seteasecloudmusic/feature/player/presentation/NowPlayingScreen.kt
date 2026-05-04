package com.example.seteasecloudmusic.feature.player.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.seteasecloudmusic.core.player.PlaybackState
import com.example.seteasecloudmusic.core.player.PlayerStatus
import com.example.seteasecloudmusic.feature.player.presentation.component.LyricsScreen
import com.example.seteasecloudmusic.feature.player.presentation.component.PlayerBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 页面常量
private object NowPlayingPage {
    const val Album = "Album"
    const val Lyric = "Lyric"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    playbackState: PlaybackState,
    lyricsState: LyricsUiState,
    currentPosition: Int,
    activeLineIndex: Int,
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onFavoriteClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onKaraokeClick: () -> Unit = {},
    onQueueClick: () -> Unit = {},
    onAudioOutputClick: () -> Unit = {}
) {
    val currentTrack = playbackState.currentTrack
    val isPlaying = playbackState.status == PlayerStatus.PLAYING
    val duration = playbackState.durationMs.coerceAtLeast(1)
    val position = playbackState.currentPositionMs.coerceIn(0, duration)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 页面状态
    var nowPage by rememberSaveable { mutableStateOf(NowPlayingPage.Album) }

    // 控制区显示状态
    var showControl by rememberSaveable { mutableStateOf(true) }

    // 翻译显示状态
    var showTranslation by rememberSaveable { mutableStateOf(true) }

    // 歌词页动画
    val alphaAnim = remember { Animatable(0f) }
    LaunchedEffect(nowPage) {
        val targetAlpha = if (nowPage == NowPlayingPage.Lyric) 1f else 0f
        scope.launch {
            alphaAnim.animateTo(targetAlpha)
        }
    }

    // 触摸超时自动隐藏控制区（歌词页）
    LaunchedEffect(showControl, nowPage) {
        if (nowPage == NowPlayingPage.Lyric && showControl) {
            delay(2500)
            showControl = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ===== Layer 0: 流光背景 =====
        PlayerBackground(
            coverUrl = currentTrack?.coverUrl,
            isPlaying = isPlaying
        )

        // ===== Layer 1: 小把手 =====
        Column(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(width = 32.dp, height = 4.5.dp)
                        .background(
                            Color(0x4DFFFFFF),
                            RoundedCornerShape(2.25.dp)
                        )
                )
            }
        }

        // ===== Layer 2: 主内容区（Crossfade 切换） =====
        Crossfade(
            targetState = nowPage,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 22.dp)
        ) { currentPage ->
            when (currentPage) {
                NowPlayingPage.Album -> {
                    // 封面页布局
                    Column(
                        Modifier
                            .fillMaxSize()
                            .clickable(enabled = false, onClick = {})
                    ) {
                        // 大封面 - Apple Music 风格正方形居中
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            val springSpec = remember {
                                SpringSpec<Float>(
                                    stiffness = 300f,
                                    dampingRatio = 1f,
                                    visibilityThreshold = 0.001f
                                )
                            }
                            val tweenSpec = remember {
                                TweenSpec<Float>(
                                    durationMillis = 350,
                                    easing = EaseOutQuart
                                )
                            }
                            val scale by animateFloatAsState(
                                targetValue = if (isPlaying) 0f else 1f,
                                animationSpec = if (isPlaying) springSpec else tweenSpec,
                                visibilityThreshold = 0.001f
                            )

                            val sidePad = (24 + 12 * scale).dp
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = sidePad)
                                    .aspectRatio(1f)
                            ) {
                                AsyncImage(
                                    model = currentTrack?.coverUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }

                        // 歌曲信息 + 操作按钮
                        AnimatedContent(
                            targetState = currentTrack,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) { track ->
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(end = 15.dp)
                                ) {
                                    Text(
                                        text = track?.title ?: "未知歌曲",
                                        fontSize = 19.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = track?.artists?.joinToString(", ") { it.name } ?: "未知艺术家",
                                        fontSize = 18.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color.White.copy(alpha = 0.35f)
                                    )
                                }

                                // 操作按钮行
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable(
                                                onClick = onFavoriteClick,
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FavoriteBorder,
                                            contentDescription = "收藏",
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable(
                                                onClick = onMoreClick,
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "更多",
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                NowPlayingPage.Lyric -> {
                    // 歌词页布局
                    Column(Modifier.fillMaxSize()) {
                        // 小封面条
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.5.dp)
                                .padding(top = 22.dp)
                                .height(70.dp)
                                .clickable {
                                    nowPage = NowPlayingPage.Album
                                    showControl = true
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = currentTrack?.coverUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(69.dp)
                                    .clip(RoundedCornerShape(5.dp))
                            )
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(start = 12.dp, end = 15.dp)
                            ) {
                                Text(
                                    text = currentTrack?.title ?: "未知歌曲",
                                    fontSize = 16.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.5.sp
                                )
                                Text(
                                    text = currentTrack?.artists?.joinToString(", ") { it.name } ?: "未知艺术家",
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.White.copy(alpha = 0.35f)
                                )
                            }

                            // 操作按钮
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable(
                                            onClick = onFavoriteClick,
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FavoriteBorder,
                                        contentDescription = "收藏",
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable(
                                            onClick = onMoreClick,
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "更多",
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        // 歌词视图
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    compositingStrategy = CompositingStrategy.ModulateAlpha
                                    alpha = alphaAnim.value
                                }
                        ) {
                            LyricsScreen(
                                lyricsState = lyricsState,
                                currentPosition = currentPosition,
                                activeLineIndex = activeLineIndex,
                                isPlaying = isPlaying,
                                showTranslation = showTranslation,
                                onLineClick = onSeekTo
                            )
                        }
                    }
                }
            }
        }

        // ===== Layer 3: 音乐控制区 =====
        Box(
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                Modifier
                    .fillMaxHeight(0.437f)
                    .fillMaxWidth()
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showControl || nowPage == NowPlayingPage.Album,
                    enter = fadeIn() + expandVertically(
                        expandFrom = Alignment.Top,
                        initialHeight = { (it / 1.4).toInt() }
                    ),
                    exit = fadeOut() + shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        targetHeight = { (it / 1.4).toInt() }
                    )
                ) {
                    // 翻译切换按钮（仅歌词页显示）
                    if (nowPage == NowPlayingPage.Lyric) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                                .graphicsLayer {
                                    compositingStrategy = CompositingStrategy.ModulateAlpha
                                    alpha = alphaAnim.value
                                },
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .alpha(0.4f)
                                    .clickable(
                                        onClick = { showTranslation = !showTranslation },
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedContent(
                                    targetState = showTranslation,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                                ) { show ->
                                    Text(
                                        text = "译",
                                        fontSize = 18.sp,
                                        fontWeight = if (show) FontWeight.Bold else FontWeight.Normal,
                                        color = Color.White.copy(alpha = if (show) 1f else 0.4f)
                                    )
                                }
                            }
                        }
                    }

                    PlayerControl(
                        isPlaying = isPlaying,
                        position = position,
                        duration = duration,
                        onPlayPause = onPlayPause,
                        onPrevious = onPrevious,
                        onNext = onNext,
                        onSeekTo = onSeekTo,
                        nowPage = nowPage,
                        onLyricsClick = {
                            if (nowPage == NowPlayingPage.Lyric) {
                                nowPage = NowPlayingPage.Album
                            } else {
                                nowPage = NowPlayingPage.Lyric
                            }
                            showControl = true
                        },
                        onPlaylistClick = onQueueClick,
                        modifier = Modifier.padding(top = 52.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerControl(
    isPlaying: Boolean,
    position: Int,
    duration: Int,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeekTo: (Int) -> Unit,
    nowPage: String,
    onLyricsClick: () -> Unit,
    onPlaylistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sliderPosition = remember { mutableFloatStateOf(position.toFloat()) }
    val isSliding = remember { mutableStateOf(false) }

    // 更新滑块位置
    LaunchedEffect(position) {
        if (!isSliding.value) {
            sliderPosition.floatValue = position.toFloat()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp)
            .padding(bottom = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 进度条
        Slider(
            value = sliderPosition.floatValue,
            onValueChange = { newValue ->
                isSliding.value = true
                sliderPosition.floatValue = newValue
            },
            onValueChangeFinished = {
                onSeekTo(sliderPosition.floatValue.toInt())
                isSliding.value = false
            },
            valueRange = 0f..duration.toFloat().coerceAtLeast(0f),
            colors = SliderDefaults.colors(
                activeTrackColor = Color.White,
                inactiveTrackColor = Color(0x0DFFFFFF)
            ),
            modifier = Modifier
                .alpha(0.45f)
                .height(14.dp),
            thumb = {}
        )

        // 时间标签
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 7.dp)
                .height(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(position.toLong() / 1000),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp,
                    color = Color.White.copy(alpha = 0.3f)
                )
                Text(
                    text = "-${formatTime((duration - position).toLong() / 1000)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp,
                    color = Color.White.copy(alpha = 0.3f)
                )
            }
        }

        // 播放控制按钮
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 上一首
                Box(
                    modifier = Modifier
                        .size(61.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onPrevious
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_media_previous),
                        contentDescription = "上一首",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.width(43.dp))

                // 播放/暂停
                Box(
                    modifier = Modifier
                        .size(58.5.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onPlayPause
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isPlaying,
                        transitionSpec = {
                            (scaleIn(initialScale = 0.3f) + fadeIn()).togetherWith(
                                scaleOut(targetScale = 0.3f) + fadeOut()
                            )
                        }
                    ) { playing ->
                        Icon(
                            painter = painterResource(
                                id = if (playing) android.R.drawable.ic_media_pause
                                else android.R.drawable.ic_media_play
                            ),
                            contentDescription = if (playing) "暂停" else "播放",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(43.dp))

                // 下一首
                Box(
                    modifier = Modifier
                        .size(61.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onNext
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_media_next),
                        contentDescription = "下一首",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    )
                }
            }
        }

        // 底部栏（歌词、播放列表）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.4f),
            horizontalArrangement = Arrangement.Center
        ) {
            val dp = 32.dp

            // 歌词按钮
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .weight(1f)
                    .clickable(
                        onClick = onLyricsClick,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = nowPage == NowPlayingPage.Lyric,
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { isLyric ->
                    Icon(
                        painter = painterResource(
                            id = if (isLyric) android.R.drawable.ic_menu_edit
                            else android.R.drawable.ic_menu_view
                        ),
                        contentDescription = "歌词",
                        modifier = Modifier.size(dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.2f))

            // 播放列表按钮
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .weight(1f)
                    .clickable(
                        onClick = onPlaylistClick,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_agenda),
                    contentDescription = "播放列表",
                    modifier = Modifier.size(dp)
                )
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "$minutes:${if (secs < 10) "0$secs" else "$secs"}"
}
