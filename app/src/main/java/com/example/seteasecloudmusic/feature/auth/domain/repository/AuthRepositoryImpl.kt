package com.example.seteasecloudmusic.feature.auth.domain.repository

import android.content.Context
import com.example.seteasecloudmusic.feature.auth.data.AuthService
import com.example.seteasecloudmusic.feature.auth.data.model.LoginResponse
import com.example.seteasecloudmusic.feature.auth.domain.model.AuthSession
import com.example.seteasecloudmusic.feature.auth.domain.model.LoginMethod
import com.example.seteasecloudmusic.feature.auth.domain.model.QrLoginStart
import com.example.seteasecloudmusic.feature.auth.domain.model.QrPollResult
import com.example.seteasecloudmusic.feature.auth.domain.model.QrStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    @ApplicationContext private val context: Context
) : AuthRepository {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow(loadSessionFromPrefs())

    override fun observeAuthState(): StateFlow<AuthSession?> = _authState.asStateFlow()

    override suspend fun loginByPhone(phone: String, password: String): Result<AuthSession> {
        return runCatching {
            val response = authService.loginWithPassword(phone, password)
            parseLoginResponse(response, LoginMethod.PHONE)
        }
    }

    override suspend fun loginByEmail(email: String, password: String): Result<AuthSession> {
        return Result.failure(NotImplementedError("邮箱登录暂不支持"))
    }

    override suspend fun loginByCaptcha(phone: String, captcha: String): Result<AuthSession> {
        return runCatching {
            val response = authService.loginWithCaptcha(phone, captcha)
            parseLoginResponse(response, LoginMethod.CAPTCHA)
        }
    }

    override suspend fun startQrLogin(): Result<QrLoginStart> {
        return runCatching {
            val keyResponse = authService.getQrKey(System.currentTimeMillis())
            if (!keyResponse.isSuccessful) {
                throw Exception("获取 QR Key 失败: ${keyResponse.code()}")
            }
            val unikey = keyResponse.body()?.data?.unikey
                ?: throw Exception("QR Key 为空")

            val codeResponse = authService.getQrCode(
                key = unikey,
                timestamp = System.currentTimeMillis(),
                qrimg = 1
            )
            if (!codeResponse.isSuccessful) {
                throw Exception("获取 QR Code 失败: ${codeResponse.code()}")
            }
            val data = codeResponse.body()?.data
            QrLoginStart(
                key = unikey,
                qrUrl = data?.qrurl,
                qrImageBase64 = data?.qrimg
            )
        }
    }

    override suspend fun pollQrStatus(key: String): Result<QrPollResult> {
        return runCatching {
            val response = authService.checkQrCodeStatus(key, System.currentTimeMillis())
            if (!response.isSuccessful) {
                throw Exception("轮询 QR 状态失败: ${response.code()}")
            }
            val body = response.body()
                ?: throw Exception("QR 状态响应体为空")

            when (body.code) {
                801 -> QrPollResult(state = QrStatus.WAIT_SCAN, message = body.message)
                802 -> QrPollResult(
                    state = QrStatus.WAIT_CONFIRM,
                    message = body.message.ifBlank { "扫码成功，请在手机上确认" }
                )
                803 -> {
                    val session = AuthSession(
                        cookie = body.cookie.takeIf { it.isNotBlank() },
                        loginMethod = LoginMethod.QR,
                        isLoggedIn = true
                    )
                    saveSession(session)
                    QrPollResult(state = QrStatus.SUCCESS, session = session, message = "登录成功")
                }
                800 -> QrPollResult(state = QrStatus.EXPIRED, message = "二维码已过期")
                else -> QrPollResult(state = QrStatus.EXPIRED, message = body.message.ifBlank { "未知状态" })
            }
        }
    }

    override suspend fun guestLogin(): Result<AuthSession> {
        clearSession()
        return Result.success(AuthSession(loginMethod = LoginMethod.GUEST))
    }

    override suspend fun refreshSessionIfNeeded(): Result<AuthSession> {
        val current = loadSessionFromPrefs()
        return if (current?.isLoggedIn == true) {
            Result.success(current)
        } else {
            Result.failure(Exception("无有效登录态"))
        }
    }

    override suspend fun sendCaptcha(phone: String): Result<Unit> {
        return runCatching {
            val response = authService.sendCaptcha(phone)
            if (!response.isSuccessful) {
                throw Exception("发送验证码失败: ${response.code()}")
            }
            val body = response.body()
            if (body?.code != 200) {
                throw Exception(body?.message ?: "发送验证码失败")
            }
        }
    }

    private fun parseLoginResponse(response: Response<LoginResponse>, method: LoginMethod): AuthSession {
        if (!response.isSuccessful) {
            throw Exception("登录请求失败: ${response.code()}")
        }
        val body = response.body()
            ?: throw Exception("登录响应体为空")
        if (body.code != 200) {
            throw Exception("登录失败: code=${body.code}")
        }

        val profile = body.profile
        val account = body.account

        val session = AuthSession(
            userId = profile?.userId ?: account?.id,
            nickname = profile?.nickname,
            avatarUrl = profile?.avatarUrl,
            cookie = body.cookie,
            loginMethod = method,
            isLoggedIn = true
        )
        saveSession(session)
        return session
    }

    private fun saveSession(session: AuthSession) {
        prefs.edit().apply {
            putLong(KEY_USER_ID, session.userId ?: -1L)
            putString(KEY_NICKNAME, session.nickname)
            putString(KEY_AVATAR_URL, session.avatarUrl)
            putString(KEY_COOKIE, session.cookie)
            putString(KEY_LOGIN_METHOD, session.loginMethod.name)
            putBoolean(KEY_IS_LOGGED_IN, session.isLoggedIn)
            apply()
        }
        _authState.value = session
    }

    private fun loadSessionFromPrefs(): AuthSession? {
        val cookie = prefs.getString(KEY_COOKIE, null)
        if (cookie.isNullOrBlank()) return null

        val userId = prefs.getLong(KEY_USER_ID, -1L).takeIf { it != -1L }
        val nickname = prefs.getString(KEY_NICKNAME, null)
        val avatarUrl = prefs.getString(KEY_AVATAR_URL, null)
        val methodName = prefs.getString(KEY_LOGIN_METHOD, LoginMethod.UNKNOWN.name)
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

        return AuthSession(
            userId = userId,
            nickname = nickname,
            avatarUrl = avatarUrl,
            cookie = cookie,
            loginMethod = LoginMethod.valueOf(methodName ?: LoginMethod.UNKNOWN.name),
            isLoggedIn = isLoggedIn
        )
    }

    private fun clearSession() {
        prefs.edit().clear().apply()
        _authState.value = null
    }

    companion object {
        private const val PREF_NAME = "auth_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val KEY_COOKIE = "cookie"
        private const val KEY_LOGIN_METHOD = "login_method"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
}
