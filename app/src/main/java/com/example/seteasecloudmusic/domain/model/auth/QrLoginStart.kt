package com.example.seteasecloudmusic.domain.model.auth

data class QrLoginStart(
    val key: String,
    val qrUrl: String? = null,
    val qrImageBase64: String? = null
)