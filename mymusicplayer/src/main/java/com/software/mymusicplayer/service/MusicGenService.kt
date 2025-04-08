package com.software.mymusicplayer.service

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface MusicGenService {
    @POST("generate")
    fun generateAudio(
        @Body text: AudioRequest
    ): Call<AudioResponse>

    data class AudioRequest(
        @SerializedName("text")
        val promptText: String,

        @SerializedName("duration")
        val durationSeconds: Int = 30,  // 默认30秒音频

        @SerializedName("format")
        val outputFormat: String = "wav" // 默认WAV格式
    )

    data class AudioResponse(
        @SerializedName("audio_url")
        val audioUrl: String,

        @SerializedName("expire_time")
        val expireTimestamp: Long,  // 使用时间戳更规范

        @SerializedName("metadata")
        val meta: AudioMetadata
    )

    data class AudioMetadata(
        val duration: Float,
        val sampleRate: Int,
        val fileSize: Long
    )
}