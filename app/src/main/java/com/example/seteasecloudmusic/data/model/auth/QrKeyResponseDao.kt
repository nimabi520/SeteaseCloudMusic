package com.example.seteasecloudmusic.data.model.auth

/**
 * 登录二维码key接口的顶层响应。
 */
data class QrKeyResponseDao(
	val data: QrKeyDataDao? = null,
	val code: Int = 0
)

/**
 * 二维码key数据，包含唯一标识。
 */
data class QrKeyDataDao(
	val code: Int = 0,
	val unikey: String = ""
)
