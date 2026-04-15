package com.example.seteasecloudmusic.feature.auth.usecase

import com.example.seteasecloudmusic.feature.auth.domain.repository.AuthRepository

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}
