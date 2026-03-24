package com.example.seteasecloudmusic.domain.usecase.auth

import com.example.seteasecloudmusic.domain.model.auth.AuthSession
import com.example.seteasecloudmusic.domain.repository.AuthRepository

class PhoneLoginUseCase(
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(phone: String, password: String): Result<AuthSession> {
        val p = phone.trim()
        if (p.length !in 11..20) return Result.failure(IllegalArgumentException("invalid phone"))
        if (password.isBlank()) return Result.failure(IllegalArgumentException("password is blank"))
        return authRepository.loginByPhone(p, password)
    }
}