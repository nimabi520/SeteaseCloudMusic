package com.example.seteasecloudmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
            AppNavigation()
        }
    }
}