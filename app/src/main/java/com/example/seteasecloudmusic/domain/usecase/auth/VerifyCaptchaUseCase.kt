package com.example.seteasecloudmusic.domain.usecase.auth

import com.example.seteasecloudmusic.domain.model.auth.AuthSession
import com.example.seteasecloudmusic.domain.repository.AuthRepository

class VerifyCaptchaUseCase (
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String,captcha: String): Result<AuthSession> {
        val p = phone.trim()
        val c = captcha.trim()

        if (p.length !in 11..20) return Result.failure(IllegalArgumentException("invalid phone"))
        if (c.length != 6 || !c.all { it in '0'..'9' }) {
            return Result.failure(IllegalArgumentException("captcha must be exactly 6 digits"))
        }

        return authRepository.loginByCaptcha(p, captcha)
    }
}