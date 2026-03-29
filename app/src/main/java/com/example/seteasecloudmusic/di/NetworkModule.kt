package com.example.seteasecloudmusic.di

import com.example.seteasecloudmusic.data.api.NeteaseMusicService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * `di` 模块说明：
 *
 * 这一层负责“组装依赖”，也就是把网络、仓库、用例等对象按需要创建出来。
 * 当前文件先手动承担了简单依赖注入的职责，后续如果接入 Hilt/Koin，
 * 这里的思路仍然一样，只是写法会更框架化。
 *
 * `NetworkModule` 当前主要负责：
 * 1. 定义服务端基础地址。
 * 2. 配置 OkHttp 超时等网络参数。
 * 3. 创建 Retrofit 并产出 `NeteaseMusicService`。
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
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            // TODO: 以后可在这里补登录态、日志、统一错误处理等拦截器。
            .build()
    }

    /**
     * 构建 Retrofit 实例并挂载 Gson 转换器。
     */
    private fun provideRetrofit(client: OkHttpClient): Retrofit{
        return Retrofit.Builder()
            .baseUrl(provideBaseUrl())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
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
