package com.example.seteasecloudmusic.data.model.auth

/**
 * 登录二维码状态检查接口的响应。
 */
data class QrStatusResponse(
	val code: Int = 0,
	val message: String = "",
	val cookie: String = ""
)