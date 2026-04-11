package com.example.seteasecloudmusic.feature.auth.data.model

/**
 * 登录二维码创建接口的响应。
 */
data class QrCodeResponse(
	val code: Int = 0,
	val data: QrCodeDataResponse? = null
)

/**
 * 二维码数据，包含二维码URL和Base64图片。
 */
data class QrCodeDataResponse(
	val qrurl: String = "",
	val qrimg: String = ""
)