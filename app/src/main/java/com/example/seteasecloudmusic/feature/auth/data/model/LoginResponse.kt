package com.example.seteasecloudmusic.feature.auth.data.model

/**
 * 登录接口返回体。
 */
data class LoginResponse(
    val code: Int = 0,
    val account: AccountResponse? = null,
    val profile: ProfileResponse? = null,
    val cookie: String? = null,
    val token: String? = null
)

data class AccountResponse(
    val id: Long
)

data class ProfileResponse(
    val userId: Long,
    val nickname: String,
    val avatarUrl: String
)