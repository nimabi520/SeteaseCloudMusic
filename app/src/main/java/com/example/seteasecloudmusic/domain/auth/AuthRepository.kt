package com.example.seteasecloudmusic.domain.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository{

    suspend fun loginByPhone(
        phone: String,
        password: String
    ): Result<AuthSession>

    suspend fun loginByEmail(
        email: String,
        passwaor: String
    ): Result<AuthSession>

    suspend fun loginByCaptcha(
        phone: String,
        captcha: String
    ): Result<AuthSession>

    //启动二维码登录流程：返回 key + 展示用二维码信息。
    suspend fun startQrlogin(): Result<QrLoginStart>

    /**
     * 轮询二维码状态：
     * - 801 等待扫码
     * - 802 待确认
     * - 803 登录成功
     * - 800 二维码过期
     */
    suspend fun pollQrStatus(key: String): Result<QrPollResult>

    suspend fun guestLogin(): Result<AuthSession>

    suspend fun refreshSessionIfNeeded(): Result<AuthSession>

    //订阅当前登录态；null 代表未登录。
    fun observeAuthState(): Flow<AuthSession?>
}

//二维码登录启动信息（给 UI 展示二维码）。
data class QrLoginStart(
    val key:String,
    val qrUrl: String? = null,
    val qrImageBase64: String? = null
)

//二维码轮询结果
data class QrPollResult(
    val state: QrStatus,
    val session: AuthSession? = null,
    val message: String? = null
)

data class AuthSession(
    val userId: Long? = null,
    val nickname: String? = null,
    val cookie: String? = null,
    val loginMethod: LoginMethod = LoginMethod.UNKNOWN,
    val isLoggedIn: Boolean = false
)

enum class LoginMethod{
    PHONE,
    EMAIL,
    CAPTCHA,
    QR,
    GUEST,
    UNKNOWN
}

enum class QrStatus{
    WAIT_SCAN,      // 801
    WAIT_CONFIRM,   // 802
    SUCCESS,        // 803
    EXPIRED         // 800
}