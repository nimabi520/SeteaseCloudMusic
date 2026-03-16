package com.example.seteasecloudmusic.data.api.auth

import retrofit2.http.POST
import retrofit2.http.Query
import com.example.seteasecloudmusic.data.model.auth.LoginResponseDto
import com.example.seteasecloudmusic.data.model.auth.QrKeyResponseDto
import com.example.seteasecloudmusic.data.model.auth.QrCodeResponseDto
import com.example.seteasecloudmusic.data.model.auth.QrStatusResponseDto
import java.sql.Timestamp

/**
 * 认证接口定义占位，后续用于登录与会话相关 API。
 */
interface AuthService {
    @POST("/login/cellphone")
    suspend fun loginWithPassword(
        @Query("phone") phone: String,
        @Query("password") password: String
    ): LoginResponseDto

    @POST("/login/qr/key")
    suspend fun getQrKey (
        @Query("timestamp")timestamp: Timestamp
    ): QrKeyResponseDto

    @POST("/login/qr/create")
    suspend fun getQrCode(
        @Query("key")key: String,
        @Query("timestamp")timestamp: Timestamp,
        @Query("qrimg")qrimg: Int
    ): QrCodeResponseDto

    @POST("/login/qr/check")
    suspend fun checkQrCodeStatus(
        @Query("key")key: String,
        @Query("timestamp")timestamp: Timestamp
    ): QrStatusResponseDto


}