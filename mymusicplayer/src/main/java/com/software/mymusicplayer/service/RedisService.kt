package com.software.mymusicplayer.service

import android.provider.CalendarContract.CalendarAlerts
import com.software.mymusicplayer.entity.BaseResponse
import com.software.mymusicplayer.entity.BaseStatu
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RedisService {
    @GET("string/{key}")
    fun get(@Path("key")key:String):Call<BaseResponse<String>>
    @POST("string/{key}")
    fun setEx(@Path("key")key:String,
              @Query("value")value:String,@Query("expire")ex:Long):Call<BaseStatu<String>>
    @GET("scard/{key}")
    fun sCard(@Path("key")key:String):Call<BaseResponse<String>>
    @POST("sadd/{key}")
    fun sAdd(@Path("key") key: String,@Query("member")member:String):Call<BaseResponse<String>>
    @POST("zadd/{key}")
    fun zAdd(@Path("key")key:String,@Body members:Map<String,Double>):Call<BaseStatu<String>>
    @GET("zcard/{key}")
    fun zCard(@Path("key")key:String):Call<BaseResponse<String>>
    @GET("del/{key}")
    fun del(@Path("key")key:String):Call<BaseResponse<String>>
    @GET("zset/{key}/range")
    fun zRange(@Path("key")key:String,
               @Query("min")min:Int,@Query("max")max:Int) :Call<Set<String>>
    @GET("zset/{key}/revrange")
    fun zRevRange(@Path("key")key:String,
               @Query("min")min:Int,@Query("max")max:Int) :Call<Set<String>>
    @POST("zset/zincrby/{key}")
    fun zIncBy(@Path("key")key: String,
               @Query("value")value: Double,
               @Query("member")members: String):Call<BaseResponse<String>>
    @GET("sismember/{key}")
    fun sIsMember(@Path("key")key:String,@Query("member")member:String):Call<BaseResponse<String>>
    @POST("srem/{key}")
    fun sRem(@Path("key")key:String,@Query("member")member:String):Call<BaseResponse<String>>
    @GET("smembers/{key}")
    fun sMembers(@Path("key")key:String):Call<Set<String>>


}