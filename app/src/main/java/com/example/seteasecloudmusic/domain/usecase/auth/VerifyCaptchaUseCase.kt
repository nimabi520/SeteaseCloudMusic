package com.example.seteasecloudmusic.domain.usecase.auth

import com.example.seteasecloudmusic.domain.model.auth.AuthSession
import com.example.seteasecloudmusic.domain.repository.AuthRepository

class VerifyCaptchaUseCase (
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String,captcha: String): Result<AuthSession> {
        val p = AuthInputValidator.normalizePhone(phone)
        val c = AuthInputValidator.normalizeCaptcha(captcha)

        if (!AuthInputValidator.isValidCnPhone(p)) {
            return Result.failure(IllegalArgumentException("invalid phone"))
        }
        if (!AuthInputValidator.isValidCaptcha(c)) {
            return Result.failure(IllegalArgumentException("captcha must be exactly 6 digits"))
        }

        return authRepository.loginByCaptcha(p, c)
    }
}