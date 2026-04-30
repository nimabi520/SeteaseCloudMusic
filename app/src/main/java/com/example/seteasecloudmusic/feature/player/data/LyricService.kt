package com.example.seteasecloudmusic.feature.player.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface LyricService {

    @GET("lyric")
    suspend fun getLyrics(
        @Query("id") id: Long
    ): NeteaseLyricResponse

    @GET("lyric/new")
    suspend fun getYrcLyrics(
        @Query("id") id: Long
    ): NeteaseLyricResponse
}

data class NeteaseLyricResponse(
    @SerializedName("sgc") val sgc: Boolean = false,
    @SerializedName("sfy") val sfy: Boolean = false,
    @SerializedName("qfy") val qfy: Boolean = false,
    @SerializedName("lrc") val lrc: LrcData? = null,
    @SerializedName("klyric") val klyric: LrcData? = null,
    @SerializedName("tlyric") val tlyric: LrcData? = null,
    @SerializedName("romalrc") val romalrc: LrcData? = null,
    @SerializedName("yrc") val yrc: LrcData? = null,
    @SerializedName("code") val code: Int = 0
)

data class LrcData(
    @SerializedName("version") val version: Int = 0,
    @SerializedName("lyric") val lyric: String? = null
)
