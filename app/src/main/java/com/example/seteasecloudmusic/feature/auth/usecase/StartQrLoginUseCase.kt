package com.example.seteasecloudmusic.feature.auth.usecase

import com.example.seteasecloudmusic.feature.auth.domain.model.QrLoginStart
import com.example.seteasecloudmusic.feature.auth.domain.repository.AuthRepository

class StartQrLoginUseCase (
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<QrLoginStart> {
        return authRepository.startQrLogin()
    }
}