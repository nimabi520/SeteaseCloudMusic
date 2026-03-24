package com.example.seteasecloudmusic.domain.usecase.auth

import com.example.seteasecloudmusic.domain.repository.AuthRepository

class SendCaptchaUseCase (
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String): Result<Unit> {
        val p = AuthInputValidator.normalizePhone(phone)

        if (!AuthInputValidator.isValidCnPhone(p)) {
            return Result.failure(IllegalArgumentException("invalid phone"))
        }

        return authRepository.sendCaptcha(p)
    }
}