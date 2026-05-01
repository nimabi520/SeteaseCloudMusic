package com.example.seteasecloudmusic.feature.player.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.seteasecloudmusic.core.model.Track
import com.example.seteasecloudmusic.core.player.PlaybackState
import com.example.seteasecloudmusic.core.player.PlayerStatus
import com.example.seteasecloudmusic.feature.player.presentation.component.GlassProgressBar
import com.example.seteasecloudmusic.feature.player.presentation.component.GlassVolumeBar
import com.example.seteasecloudmusic.feature.player.presentation.component.KaraokeButton
import com.example.seteasecloudmusic.feature.player.presentation.component.LyricsScreen
import com.example.seteasecloudmusic.feature.player.presentation.component.PlayerBackground
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
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
    val pagerState = rememberPagerState(pageCount = { 2 })
    val isLyricsPage = pagerState.currentPage == 1
    val scope = rememberCoroutineScope()

    val backdrop = rememberLayerBackdrop { drawContent() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // === Background layer (registered as backdrop source) ===
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        ) {
            PlayerBackground(
                coverUrl = currentTrack?.coverUrl,
                isLyricsPage = isLyricsPage
            )
        }

        // === Swipe down to dismiss ===
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount > 30f) onClose()
                    }
                }
        )

        // === Content: Cover / Lyrics pager ===
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                0 -> CoverPage(track = currentTrack, modifier = Modifier.fillMaxSize())
                1 -> LyricsScreen(
                    lyricsState = lyricsState,
                    currentPosition = currentPosition,
                    activeLineIndex = activeLineIndex,
                    isPlaying = isPlaying,
                    onLineClick = onSeekTo,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // === Overlay: info & controls ===
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top drag handle
            DragHandle(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            )

            if (isLyricsPage) {
                TopInfoBar(
                    track = currentTrack,
                    backdrop = backdrop,
                    onFavoriteClick = onFavoriteClick,
                    onMoreClick = onMoreClick,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.statusBarsPadding())
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!isLyricsPage) {
                CoverBottomInfo(
                    track = currentTrack,
                    backdrop = backdrop,
                    onFavoriteClick = onFavoriteClick,
                    onMoreClick = onMoreClick,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            BottomControlsGlass(
                playbackState = playbackState,
                backdrop = backdrop,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
                onSeekTo = onSeekTo,
                onQueueClick = onQueueClick,
                onAudioOutputClick = onAudioOutputClick,
                onLyricsClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            )
        }

        // Floating karaoke button (lyrics page only)
        if (isLyricsPage) {
            KaraokeButton(
                onClick = onKaraokeClick,
                backdrop = backdrop,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 260.dp)
            )
        }
    }
}

@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 40.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.White.copy(alpha = 0.35f))
    )
}

@Composable
private fun TopInfoBar(
    track: Track?,
    backdrop: com.kyant.backdrop.Backdrop,
    onFavoriteClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AsyncImage(
            model = track?.coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = track?.title ?: "未知歌曲",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            )
            Text(
                text = track?.artists?.joinToString(" / ") { it.name } ?: "未知歌手",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.58f),
                    fontSize = 13.sp
                )
            )
        }

        GlassCircleIconButton(
            imageVector = Icons.Default.StarBorder,
            contentDescription = "收藏",
            backdrop = backdrop,
            onClick = onFavoriteClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        GlassCircleIconButton(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "更多",
            backdrop = backdrop,
            onClick = onMoreClick
        )
    }
}

@Composable
private fun CoverPage(track: Track?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = track?.coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun CoverBottomInfo(
    track: Track?,
    backdrop: com.kyant.backdrop.Backdrop,
    onFavoriteClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track?.title ?: "未知歌曲",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            )
            Text(
                text = track?.artists?.joinToString(" / ") { it.name } ?: "未知歌手",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 15.sp
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassCircleIconButton(
                imageVector = Icons.Default.StarBorder,
                contentDescription = "收藏",
                backdrop = backdrop,
                onClick = onFavoriteClick,
                buttonSize = 42.dp,
                iconSize = 24.dp
            )
            GlassCircleIconButton(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "更多",
                backdrop = backdrop,
                onClick = onMoreClick,
                buttonSize = 42.dp,
                iconSize = 24.dp
            )
        }
    }
}

@Composable
private fun GlassCircleIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    backdrop: com.kyant.backdrop.Backdrop,
    onClick: () -> Unit,
    buttonSize: androidx.compose.ui.unit.Dp = 38.dp,
    iconSize: androidx.compose.ui.unit.Dp = 22.dp
) {
    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(CircleShape)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(buttonSize / 2) },
                effects = {
                    vibrancy()
                    blur(2.2f.dp.toPx())
                    lens(10f.dp.toPx(), 18f.dp.toPx())
                },
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.18f)) }
            )
            .background(Color.Black.copy(alpha = 0.10f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(buttonSize)) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = Color.White.copy(alpha = 0.88f),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
private fun BottomControlsGlass(
    playbackState: PlaybackState,
    backdrop: com.kyant.backdrop.Backdrop,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onQueueClick: () -> Unit,
    onAudioOutputClick: () -> Unit,
    onLyricsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val duration = playbackState.durationMs.coerceAtLeast(1)
    val position = playbackState.currentPositionMs.coerceIn(0, duration)
    val isPlaying = playbackState.status == PlayerStatus.PLAYING
    val progress = position.toFloat() / duration.toFloat()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(28.dp) },
                effects = {
                    vibrancy()
                    blur(2.8f.dp.toPx())
                    lens(
                        refractionHeight = 18f.dp.toPx(),
                        refractionAmount = 34f.dp.toPx(),
                        chromaticAberration = true
                    )
                },
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.14f)) }
            )
            .background(
                color = Color.Black.copy(alpha = 0.10f),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress bar with expanded touch area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .pointerInput(duration) {
                        detectHorizontalDragGestures { change, _ ->
                            val fraction = change.position.x / size.width
                            onSeekTo((fraction.coerceIn(0f, 1f) * duration).toInt())
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                GlassProgressBar(
                    progress = progress,
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Time labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(position),
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "不可用",
                    color = Color.White.copy(alpha = 0.38f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "-${formatTime(duration - position)}",
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Playback buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "上一首",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.17f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onPlayPause, modifier = Modifier.size(64.dp)) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "下一首",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Volume bar
            GlassVolumeBar(
                volume = 1f,
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLyricsClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "歌词",
                        tint = Color.White.copy(alpha = 0.68f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onAudioOutputClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Default.Headset,
                        contentDescription = "音频输出",
                        tint = Color.White.copy(alpha = 0.68f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onQueueClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "播放队列",
                        tint = Color.White.copy(alpha = 0.68f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
