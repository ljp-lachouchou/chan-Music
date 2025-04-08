package com.software.mymusicplayer.service

import com.software.mymusicplayer.entity.BaseBean
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface BaseBeanService {
    @Streaming
    @GET("?client_id=d3700a33&format=json&search={field}")
    fun getBaseBeanBySearch(@Path("field") field:String):Flowable<BaseBean>
//    @GET("?client_id=d3700a33&format=json&search=rock")
//    fun getRock():Flowable<BaseBean>
//    @GET("?client_id=d3700a33&format=json&search=pop")
//    fun getPop():Flowable<BaseBean>
//    @GET("?client_id=d3700a33&format=json&search=R&B")
//    fun getRAndB():Flowable<BaseBean>
//    @GET("?client_id=d3700a33&format=json&search=Jazz")
//    fun getJazz():Flowable<BaseBean>
//    @GET("?client_id=d3700a33&format=json&search=Hip-Hop")
//    fun getHipHop():Flowable<BaseBean>
    @Streaming
    @GET("?client_id=d3700a33&format=json&limit=9")
    fun getBaseBean(@Query("search") field:String,@Query("offset") offset:Int): Call<BaseBean>
    @Streaming
    @GET("?client_id=d3700a33&format=json")//&id={id}
    fun getBaseBeanBySongId(@Query("id") id:String):Call<BaseBean>
    @Streaming
    @GET("?client_id=d3700a33&format=json")
    fun getBaseBeanByAlbumId(@Query("album_id") albumId:String):Call<BaseBean>




}