package com.example.seteasecloudmusic.domain.usecase.auth

import com.example.seteasecloudmusic.domain.model.auth.AuthSession
import com.example.seteasecloudmusic.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveAuthStateUseCase (
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthSession?> {
        return authRepository.observeAuthState()
    }
}