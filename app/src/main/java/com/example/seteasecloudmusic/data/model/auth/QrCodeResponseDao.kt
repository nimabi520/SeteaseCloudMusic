package com.example.seteasecloudmusic.data.model.auth

/**
 * 登录二维码创建接口的响应。
 */
data class QrCodeResponseDao(
	val code: Int = 0,
	val data: QrCodeDataDao? = null
)

/**
 * 二维码数据，包含二维码URL和Base64图片。
 */
data class QrCodeDataDao(
	val qrurl: String = "",
	val qrimg: String = ""
)