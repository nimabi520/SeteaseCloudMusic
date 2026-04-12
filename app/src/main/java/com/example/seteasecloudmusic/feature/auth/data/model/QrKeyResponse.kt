package com.example.seteasecloudmusic.feature.auth.data.model

/**
 * 登录二维码key接口的顶层响应。
 */
data class QrKeyResponse(
	val data: QrKeyDataResponse? = null,
	val code: Int = 0
)

/**
 * 二维码key数据，包含唯一标识。
 */
data class QrKeyDataResponse(
	val code: Int = 0,
	val unikey: String = ""
)
