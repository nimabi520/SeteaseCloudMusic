package com.example.seteasecloudmusic.di

import com.example.seteasecloudmusic.data.api.NeteaseMusicService
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 网络层依赖提供者，负责构建 OkHttp、Retrofit 与服务接口。
 */
class NetworkModule {

    /**
     * API 基础地址。
     */
    private fun provideBaseUrl(): String = "http://57.158.26.135:3000/";

    /**
     * 提供统一超时配置的 HTTP 客户端。
     */
    private fun provideHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // 设置连接超时时间
            .readTimeout(30, TimeUnit.SECONDS)    // 设置读取超时时间
            .writeTimeout(30, TimeUnit.SECONDS)   // 设置写入超时时间
            //TODO:以后记得回来添加拦截器
            .build()
    }

    /**
     * 构建 Retrofit 实例并挂载 Gson 转换器。
     */
    private fun provideRetrofit(client: OkHttpClient): Retrofit{
        return Retrofit.Builder()
            .baseUrl(provideBaseUrl())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // 添加 Gson 转换器
            .build()
    }

    /**
     * 暴露音乐 API 服务实例。
     */
    fun provideNeteaseMusicService(): NeteaseMusicService {
        val client: OkHttpClient = provideHttpClient()
        val retrofit: Retrofit = provideRetrofit(client)
        return retrofit.create(NeteaseMusicService::class.java)
    }
}