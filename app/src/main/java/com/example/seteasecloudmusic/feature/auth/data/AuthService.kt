package com.example.seteasecloudmusic.feature.auth.data

import com.example.seteasecloudmusic.feature.auth.data.model.LoginResponse
import com.example.seteasecloudmusic.feature.auth.data.model.QrCodeResponse
import com.example.seteasecloudmusic.feature.auth.data.model.QrKeyResponse
import com.example.seteasecloudmusic.feature.auth.data.model.QrStatusResponse
import retrofit2.http.POST
import retrofit2.http.Query
import java.sql.Timestamp

/**
 * 认证接口定义占位，后续用于登录与会话相关 API。
 */
interface AuthService {
    @POST("/login/cellphone")
    suspend fun loginWithPassword(
        @Query("phone") phone: String,
        @Query("password") password: String
    ): LoginResponse

    @POST("/login/qr/key")
    suspend fun getQrKey (
        @Query("timestamp")timestamp: Timestamp
    ): QrKeyResponse

    @POST("/login/qr/create")
    suspend fun getQrCode(
        @Query("key")key: String,
        @Query("timestamp")timestamp: Timestamp,
        @Query("qrimg")qrimg: Int
    ): QrCodeResponse

    @POST("/login/qr/check")
    suspend fun checkQrCodeStatus(
        @Query("key")key: String,
        @Query("timestamp")timestamp: Timestamp
    ): QrStatusResponse


}