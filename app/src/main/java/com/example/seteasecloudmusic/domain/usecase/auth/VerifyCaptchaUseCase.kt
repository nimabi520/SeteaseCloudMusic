package com.example.seteasecloudmusic.domain.usecase.auth

import com.example.seteasecloudmusic.domain.model.auth.AuthSession
import com.example.seteasecloudmusic.domain.repository.AuthRepository

class VerifyCaptchaUseCase (
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String,captcha: String): Result<AuthSession> {
        val p = phone.trim()
        if (p.length !in 11..20) return Result.failure(IllegalArgumentException("invalid phone"))
        if (captcha.isBlank()) return Result.failure(IllegalArgumentException("captcha is blank"))
        return authRepository.loginByCaptcha(p, captcha)
    }
}