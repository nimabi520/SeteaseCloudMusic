package com.example.seteasecloudmusic.domain.usecase.auth

import com.example.seteasecloudmusic.domain.model.auth.QrLoginStart
import com.example.seteasecloudmusic.domain.repository.AuthRepository

class StartQrLoginUseCase (
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<QrLoginStart> {
        return authRepository.startQrLogin()
    }
}