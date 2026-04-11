package com.example.seteasecloudmusic.feature.auth.usecase

import com.example.seteasecloudmusic.feature.auth.domain.repository.AuthRepository

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