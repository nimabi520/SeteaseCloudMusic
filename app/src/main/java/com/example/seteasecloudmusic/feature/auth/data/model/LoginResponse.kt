package com.example.seteasecloudmusic.feature.auth.data.model

import com.google.gson.annotations.SerializedName

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
    val id: Long? = null
)

data class ProfileResponse(
    @SerializedName(value = "userId", alternate = ["user_id"])
    val userId: Long? = null,
    val nickname: String? = null,
    @SerializedName(value = "avatarUrl", alternate = ["avatar_url"])
    val avatarUrl: String? = null
)

/**
 * 登录态检查接口响应体。
 */
data class LoginStatusResponse(
    val code: Int = 0,
    val data: LoginStatusData? = null,
    val account: AccountResponse? = null,
    val profile: ProfileResponse? = null
)

data class LoginStatusData(
    val code: Int = 0,
    val account: AccountResponse? = null,
    val profile: ProfileResponse? = null
)