package com.software.mymusicplayer.utils

import android.util.Log
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class RedisClient private constructor(){
    val BASE_URL = "http://myredisapi.com/api/"
    companion object {
        fun getInstance():RedisClient = Holder.INSTANCE
    }
    private var retrofit: Retrofit? = null
    //kotlin默认嵌套类为静态的
    private object Holder {
        val INSTANCE:RedisClient = RedisClient()
    }
    fun <T> getService(cls:Class<T>):T =getRetrofit().create(cls)
    @Synchronized
    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            val cache = Cache(File("com/software/mymusicplayer/cache"),512L)
//            Log.i("文件存在吗", File("com/software/mymusicplayer/cache").absolutePath.toString())
            retrofit = Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(
                    GsonConverterFactory
                        .create())
                .client(
                    OkHttpClient.Builder()
                        .connectTimeout(200, TimeUnit.SECONDS)
                        .readTimeout(300, TimeUnit.SECONDS)
                        .writeTimeout(300, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
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