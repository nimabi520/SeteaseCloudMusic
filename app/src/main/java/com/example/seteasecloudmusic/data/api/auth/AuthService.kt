package com.example.seteasecloudmusic.data.api.auth

import retrofit2.http.POST
import retrofit2.http.Query
import com.example.seteasecloudmusic.data.model.auth.LoginResponseDao
import com.example.seteasecloudmusic.data.model.auth.QrKeyResponseDao
import com.example.seteasecloudmusic.data.model.auth.QrCodeResponseDao
import com.example.seteasecloudmusic.data.model.auth.QrStatusResponseDao
import java.sql.Timestamp

/**
 * 认证接口定义占位，后续用于登录与会话相关 API。
 */
interface AuthService {
    @POST("/login/cellphone")
    suspend fun loginWithPassword(
        @Query("phone") phone: String,
        @Query("password") password: String
    ): LoginResponseDao

    @POST("/login/qr/key")
    suspend fun getQrKey (
        @Query("timestamp")timestamp: Timestamp
    ): QrKeyResponseDao

    @POST("/login/qr/create")
    suspend fun getQrCode(
        @Query("key")key: String,
        @Query("timestamp")timestamp: Timestamp,
        @Query("qrimg")qrimg: Int
    ): QrCodeResponseDao

    @POST("/login/qr/check")
    suspend fun checkQrCodeStatus(
        @Query("key")key: String,
        @Query("timestamp")timestamp: Timestamp
    ): QrStatusResponseDao


}