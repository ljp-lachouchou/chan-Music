package com.software.mymusicplayer.service

import com.software.mymusicplayer.entity.ImageInfo
import com.software.mymusicplayer.entity.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ImageService {
    // 上传图片（多部分表单）
    @Multipart
    @POST("images/upload")
    fun uploadImage(
        @Part file: MultipartBody.Part
    ): Call<UploadResponse>
    @GET("images/{filename}")
    fun getImageInfo(
        @Path("filename") filename: String
    ): Call<ImageInfo>
}