package com.example.seteasecloudmusic.di

import com.example.seteasecloudmusic.data.api.NeteaseMusicService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkModule {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://57.158.26.135:3000/")
            .addConverterFactory(GsonConverterFactory.create())  // 设置解析数据时，使用的转换库
            .build()
    }

    private val musicService: NeteaseMusicService by lazy {
        retrofit.create(NeteaseMusicService::class.java)
    }

    fun provideNeteaseMusicService(): NeteaseMusicService {
        return musicService
    }
}