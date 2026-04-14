package com.example.seteasecloudmusic.feature.auth.domain.model

import com.example.seteasecloudmusic.feature.auth.domain.model.LoginMethod

data class AuthSession(
    val userId: Long? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val cookie: String? = null,
    val loginMethod: LoginMethod = LoginMethod.UNKNOWN,
    val isLoggedIn: Boolean = false
)