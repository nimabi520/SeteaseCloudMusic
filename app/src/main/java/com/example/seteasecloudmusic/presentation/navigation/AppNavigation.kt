package com.example.seteasecloudmusic.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * 应用的导航组件，定义了不同页面之间的导航路径。
 * 包含 LiquidGlass 毛玻璃效果的底栏导航（使用 Backdrop 库）
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // 创建背景层
    val backgroundColor = Color.Black
    val backdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 主导航容器
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(backgroundColor)
        ) {
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Home.route
            ) {
                composable(BottomNavItem.Home.route) {
                    HomeScreen()
                }
                composable(BottomNavItem.Radio.route) {
                    RadioScreen()
                }
                composable(BottomNavItem.Library.route) {
                    LibraryScreen()
                }
                composable(BottomNavItem.Search.route) {
                    SearchScreen()
                }
            }
        }

        // LiquidGlass 底栏（使用 Backdrop 效果）
        LiquidBottomBar(
            backdrop = backdrop,
            currentRoute = currentRoute,
            onItemClick = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    restoreState = true
                }
            }
        )
    }
}

/**
 * 主页屏幕
 */
@Composable
private fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "主页",
            fontSize = 28.sp,
            color = Color.White
        )
    }
}

/**
 * 广播屏幕
 */
@Composable
private fun RadioScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "广播",
            fontSize = 28.sp,
            color = Color.White
        )
    }
}

/**
 * 资料库屏幕
 */
@Composable
private fun LibraryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "资料库",
            fontSize = 28.sp,
            color = Color.White
        )
    }
}

/**
 * 搜索屏幕
 */
@Composable
private fun SearchScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "搜索",
            fontSize = 28.sp,
            color = Color.White
        )
    }
}