package com.example.seteasecloudmusic.domain.model.auth

data class QrPollResult(
    val state: QrStatus,
    val session: AuthSession? = null,
    val message: String? = null
)