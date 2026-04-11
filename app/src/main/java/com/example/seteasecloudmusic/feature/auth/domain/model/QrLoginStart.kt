package com.example.seteasecloudmusic.feature.auth.domain.model

data class QrLoginStart(
    val key: String,
    val qrUrl: String? = null,
    val qrImageBase64: String? = null
)