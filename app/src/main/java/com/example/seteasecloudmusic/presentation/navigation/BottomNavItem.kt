package com.example.seteasecloudmusic.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 底部导航项数据类
 */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    data object Home : BottomNavItem(
        route = "home",
        label = "主页",
        icon = Icons.Default.Home,
        contentDescription = "首页"
    )

    data object Radio : BottomNavItem(
        route = "radio",
        label = "广播",
        icon = Icons.Rounded.Radio,
        contentDescription = "广播"
    )

    data object Library : BottomNavItem(
        route = "library",
        label = "资料库",
        icon = Icons.Rounded.FavoriteBorder,
        contentDescription = "资料库"
    )

    data object Search : BottomNavItem(
        route = "search",
        label = "搜索",
        icon = Icons.Default.Search,
        contentDescription = "搜索"
    )

    companion object {
        val items = listOf(
            Home,
            Radio,
            Library,
            Search
        )
    }
}
