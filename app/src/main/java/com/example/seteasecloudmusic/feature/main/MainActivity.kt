package com.example.seteasecloudmusic.feature.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

/**
 * `presentation` 模块说明：
 *
 * 这一层只关心“界面如何展示”和“用户如何交互”，
 * 不直接处理网络细节，也不直接实现业务规则。
 *
 * `MainActivity` 是 Android 入口，职责很单一：
 * 1. 初始化 Compose 宿主。
 * 2. 打开沉浸式边到边布局。
 * 3. 把应用的根界面 `AppNavigation()` 挂载出来。
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