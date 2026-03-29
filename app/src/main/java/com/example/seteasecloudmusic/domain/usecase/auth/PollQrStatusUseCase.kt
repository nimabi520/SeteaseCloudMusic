package com.example.seteasecloudmusic.domain.usecase.auth

import com.example.seteasecloudmusic.domain.model.auth.QrPollResult
import com.example.seteasecloudmusic.domain.repository.AuthRepository

class PollQrStatusUseCase (
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(key: String): Result<QrPollResult> {
        val k = key.trim()
        if (k.isEmpty()) return Result.failure(IllegalArgumentException("qr key is blank"))
        return authRepository.pollQrStatus(k)
    }
}