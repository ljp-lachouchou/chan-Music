package com.software.mymusicplayer.utils

import android.util.Log
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.internal.http.RetryAndFollowUpInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class RetrofitClient private constructor(){
    val BASE_URL = "https://api.jamendo.com/v3.0/tracks/"
    companion object {
        fun getInstance():RetrofitClient = Holder.INSTANCE
    }
    private var retrofit: Retrofit? = null
    //kotlin默认嵌套类为静态的
    private object Holder {
        val INSTANCE:RetrofitClient = RetrofitClient()
    }
    fun <T> getService(cls:Class<T>):T =getRetrofit().create(cls)
    @Synchronized
    private fun getRetrofit():Retrofit{
        if (retrofit == null) {
            val cache = Cache(File("com/software/mymusicplayer/cache"),512L)
            Log.i("文件存在吗",File("com/software/mymusicplayer/cache").absolutePath.toString())
            retrofit = Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(
                    GsonConverterFactory
                    .create())
                .client(
                    OkHttpClient.Builder()
                        .connectTimeout(200,TimeUnit.SECONDS)
                        .readTimeout(200,TimeUnit.SECONDS)
                        .writeTimeout(200,TimeUnit.SECONDS)
                        .retryOnConnectionFailure(false)
                        .cache(cache)
//                        .addInterceptor
//                        {
//                            chain ->
//                            val request = chain.request()
//                            if (request.body == null || request.url.queryParameter("key").isNullOrEmpty()){
//                                throw IllegalArgumentException()
//                            }
//                            chain.proceed(request = request)
//                        }
                        .build()

                )
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
        }
        return retrofit!!
    }
}