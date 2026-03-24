package com.example.seteasecloudmusic.domain.model.auth

data class AuthSession(
    val userId: Long? = null,
    val nickname: String? = null,
    val cookie: String? = null,
    val loginMethod: LoginMethod = LoginMethod.UNKNOWN,
    val isLoggedIn: Boolean = false
)