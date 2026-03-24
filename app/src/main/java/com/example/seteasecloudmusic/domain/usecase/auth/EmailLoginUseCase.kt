package com.example.seteasecloudmusic.domain.usecase.auth

import com.example.seteasecloudmusic.domain.model.auth.AuthSession
import com.example.seteasecloudmusic.domain.repository.AuthRepository

class EmailLoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthSession> {
        val e = AuthInputValidator.normalizeEmail(email)

        if (!AuthInputValidator.isValidEmail(e)) {
            return Result.failure(IllegalArgumentException("invalid email"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("password is blank"))
        }

        return authRepository.loginByEmail(e, password)
    }
}