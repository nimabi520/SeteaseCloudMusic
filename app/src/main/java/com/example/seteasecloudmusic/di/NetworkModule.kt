package com.example.seteasecloudmusic.di

import com.example.seteasecloudmusic.data.api.NeteaseMusicService
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NetworkModule {

    private fun provideBaseUrl(): String = "http://57.158.26.135:3000/";

    private fun provideHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // 设置连接超时时间
            .readTimeout(30, TimeUnit.SECONDS)    // 设置读取超时时间
            .writeTimeout(30, TimeUnit.SECONDS)   // 设置写入超时时间
            //TODO:以后记得回来添加拦截器
            .build()
    }

    private fun provideRetrofit(client: OkHttpClient): Retrofit{
        return Retrofit.Builder()
            .baseUrl(provideBaseUrl())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // 添加 Gson 转换器
            .build()
    }

    fun provideNeteaseMusicService(): NeteaseMusicService {
        val client: OkHttpClient = provideHttpClient()
        val retrofit: Retrofit = provideRetrofit(client)
        return retrofit.create(NeteaseMusicService::class.java)
    }
}