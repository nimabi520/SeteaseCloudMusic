package com.example.seteasecloudmusic.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seteasecloudmusic.domain.model.SearchSuggestions
import com.example.seteasecloudmusic.domain.model.Track
import kotlinx.coroutines.flow.collectLatest

/**
 * 【第 1~2 项】SearchRoute 入口 + SearchScreenContent 主编排
 */
@Composable
fun SearchRoute(
    viewModel: SearchViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    SearchScreenContent(
        uiState = uiState,
        onQueryChanged = { viewModel.onQueryChanged(it) },
        onSearchSubmit = { viewModel.onSearchSubmit() },
        onClearQuery = { viewModel.onClearQuery() },
        onSuggestionClick = { viewModel.onSuggestionClick(it) },
        onRetryClick = { viewModel.onRetryClick() }
    )
}

/**
 * 【第 2 项】主内容编排：按状态决定显示哪些区块
 */
@Composable
fun SearchScreenContent(
    uiState: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onClearQuery: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 输入栏（总是显示）
        SearchInputBar(
            query = uiState.query,
            onQueryChanged = onQueryChanged,
            onSearchSubmit = onSearchSubmit,
            onClearQuery = onClearQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // 内容滚动区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // 判断应该显示什么内容
            when {
                // 初始空态：没输入
                shouldShowInitialEmptyState(uiState) -> {
                    InitialEmptyStateSection()
                }

                // 建议区块（仅当 query 非空 & 没有提交搜索结果时）
                shouldShowSuggestions(uiState) -> {
                    SuggestionSection(
                        suggestions = uiState.suggestions,
                        isLoading = uiState.isSuggestionLoading,
                        errorMessage = uiState.suggestionErrorMessage,
                        onSuggestionClick = onSuggestionClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 结果加载态
                uiState.isLoading -> {
                    ResultLoadingSection()
                }

                // 结果错误态
                shouldShowResultError(uiState) -> {
                    ResultErrorSection(
                        errorMessage = uiState.errorMessage ?: "搜索失败",
                        onRetryClick = onRetryClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 结果为空态
                shouldShowEmptyResults(uiState) -> {
                    EmptyResultsSection()
                }

                // 结果区块
                shouldShowResults(uiState) -> {
                    SearchResultSection(
                        tracks = uiState.tracks,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * 【第 3 项】搜索输入框
 *
 * 处理交互：
 * - 输入变化 -> onQueryChanged
 * - 搜索提交 -> onSearchSubmit
 * - 清空输入 -> onClearQuery
 */
@Composable
fun SearchInputBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    Row(
        modifier = modifier
            .height(48.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 搜索图标
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "搜索",
            tint = Color.Gray,
            modifier = Modifier
                .size(20.dp)
                .padding(end = 8.dp)
        )

        // 输入框
        BasicTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Enter) {
                        onSearchSubmit()
                        true
                    } else {
                        false
                    }
                },
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "搜索音乐、歌手...",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )

        // 清空按钮
        if (query.isNotEmpty()) {
            Icon(
                imageVector = Icons.Filled.Clear,
                contentDescription = "清空",
                tint = Color.Gray,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onClearQuery() }
            )
        }
    }
}

/**
 * 【第 5 项】结果列表区块 + 单项行
 */
@Composable
fun SearchResultSection(
    tracks: List<Track>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(tracks) { track ->
            TrackItemRow(track = track, modifier = Modifier.fillMaxWidth())
        }
    }
}

/**
 * 单条歌曲项
 *
 * 显示：封面、标题、歌手、时长、品质标签
 */
@Composable
fun TrackItemRow(
    track: Track,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { }  // 后续接"点击播放"
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 封面占位/图片
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFEEEEEE), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎵",
                fontSize = 24.sp
            )
        }

        // 文字信息
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 标题
            Text(
                text = track.title,
                fontSize = 14.sp,
                color = Color.Black,
                maxLines = 1
            )

            // 歌手 + 时长
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = track.artists.joinToString("/") { it.name },
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                // 时长
                if (track.durationMs != null) {
                    Text(
                        text = formatDuration(track.durationMs),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // 品质标签
            if (track.qualityTags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    track.qualityTags.take(2).forEach { quality ->
                        Text(
                            text = quality.name,
                            fontSize = 10.sp,
                            color = Color(0xFFFA233B),
                            modifier = Modifier
                                .background(
                                    Color(0xFFFFE5E9),
                                    RoundedCornerShape(2.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // 可播放指示
        if (!track.isPlayable) {
            Text(
                text = "❌",
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 【第 8 项】建议区块
 *
 * 显示：歌曲、歌手、歌单、全匹配建议
 * 交互：点击建议词 -> onSuggestionClick(keyword)
 */
@Composable
fun SuggestionSection(
    suggestions: SearchSuggestions,
    isLoading: Boolean,
    errorMessage: String?,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        // 建议加载态
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
            return@Column
        }

        // 建议错误提示
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
            return@Column
        }

        // 全匹配建议
        if (suggestions.allMatch.isNotEmpty()) {
            Text(
                text = "搜索",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            suggestions.allMatch.forEach { keyword ->
                SuggestionItem(
                    text = keyword,
                    onClick = { onSuggestionClick(keyword) }
                )
            }
        }

        // 歌曲建议
        if (suggestions.songs.isNotEmpty()) {
            Text(
                text = "歌曲",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
            )
            suggestions.songs.take(3).forEach { track ->
                SuggestionTrackItem(
                    track = track,
                    onClick = { onSuggestionClick(track.title) }
                )
            }
        }

        // 歌手建议
        if (suggestions.artists.isNotEmpty()) {
            Text(
                text = "歌手",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
            )
            suggestions.artists.take(3).forEach { artist ->
                SuggestionItem(
                    text = artist.name,
                    onClick = { onSuggestionClick(artist.name) }
                )
            }
        }

        // 歌单建议
        if (suggestions.playlists.isNotEmpty()) {
            Text(
                text = "歌单",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
            )
            suggestions.playlists.take(3).forEach { playlist ->
                SuggestionItem(
                    text = playlist.name,
                    onClick = { onSuggestionClick(playlist.name) }
                )
            }
        }
    }
}

/**
 * 单个建议项（文本型）
 */
@Composable
fun SuggestionItem(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier
                .size(16.dp)
                .padding(end = 8.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

/**
 * 建议歌曲项（带艺术家信息）
 */
@Composable
fun SuggestionTrackItem(
    track: Track,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier
                .size(16.dp)
                .padding(end = 8.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                fontSize = 14.sp,
                color = Color.Black
            )
            Text(
                text = track.artists.joinToString("/") { it.name },
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * 【第 6 项】结果加载态
 */
@Composable
fun ResultLoadingSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(strokeWidth = 3.dp)
            Text(
                text = "正在搜索...",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * 【第 7 项】结果错误态 + 重试
 */
@Composable
fun ResultErrorSection(
    errorMessage: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "❌",
                fontSize = 48.sp
            )
            Text(
                text = errorMessage,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Button(onClick = onRetryClick) {
                Text("重试")
            }
        }
    }
}

/**
 * 【第 10 项】初始空态
 */
@Composable
fun InitialEmptyStateSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🎵",
                fontSize = 48.sp
            )
            Text(
                text = "输入关键词开始搜索",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * 【第 10 项】无结果空态
 */
@Composable
fun EmptyResultsSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🔍",
                fontSize = 48.sp
            )
            Text(
                text = "暂无搜索结果",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * 【第 11 项】派生状态判断函数
 * 这些函数让主编排逻辑更清晰，避免复杂的 when 分支
 */

fun shouldShowInitialEmptyState(uiState: SearchUiState): Boolean =
    uiState.query.isEmpty()

fun shouldShowSuggestions(uiState: SearchUiState): Boolean =
    uiState.query.isNotEmpty() &&
            uiState.tracks.isEmpty() &&
            (uiState.suggestions.isNotEmpty() || uiState.isSuggestionLoading || uiState.suggestionErrorMessage != null)

fun shouldShowResults(uiState: SearchUiState): Boolean =
    uiState.query.isNotEmpty() && uiState.tracks.isNotEmpty()

fun shouldShowResultError(uiState: SearchUiState): Boolean =
    uiState.query.isNotEmpty() && !uiState.isLoading && uiState.errorMessage != null && uiState.tracks.isEmpty()

fun shouldShowEmptyResults(uiState: SearchUiState): Boolean =
    uiState.query.isNotEmpty() && !uiState.isLoading && uiState.errorMessage == null && uiState.tracks.isEmpty()

/**
 * 工具函数
 */

fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / 1000 / 60) % 60
    val hours = durationMs / 1000 / 60 / 60
    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%d:%02d", minutes, seconds)
    }
}

fun SearchSuggestions.isNotEmpty(): Boolean =
    songs.isNotEmpty() || artists.isNotEmpty() || playlists.isNotEmpty() || allMatch.isNotEmpty()