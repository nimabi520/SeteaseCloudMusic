package com.example.seteasecloudmusic.feature.auth.domain.model

import com.example.seteasecloudmusic.feature.auth.domain.model.QrStatus

data class QrPollResult(
    val state: QrStatus,
    val session: AuthSession? = null,
    val message: String? = null
)