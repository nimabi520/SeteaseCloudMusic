package com.example.seteasecloudmusic.feature.auth.usecase

object AuthInputValidator {
    // 中国大陆手机号
    private val CN_PHONE_REGEX = Regex("^1[3-9]\\d{9}$")

    // 邮箱格式
    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]{1,64}@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$"
    )

    fun normalizePhone(phone: String): String = phone.trim()

    fun normalizeEmail(email: String): String = email.trim()

    fun normalizeCaptcha(captcha: String): String = captcha.trim()

    fun isValidCnPhone(phone: String): Boolean {
        return CN_PHONE_REGEX.matches(phone)
    }

    fun isValidEmail(email: String): Boolean {
        if (email.length > 254) return false
        if (!EMAIL_REGEX.matches(email)) return false
        if (email.contains("..")) return false // 禁止连续点
        return true
    }

    fun isValidCaptcha(captcha: String): Boolean {
        return captcha.length == 6 && captcha.all { it in '0'..'9' }
    }
}