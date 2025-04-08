package com.software.mymusicplayer

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.software.mymusicplayer.entity.SongList.Album
import com.software.mymusicplayer.entity.Artist
import com.software.mymusicplayer.entity.BaseBean
import com.software.mymusicplayer.entity.BaseResponse
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.manager.NoteDatabaseManager
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.BaseBeanService
import com.software.mymusicplayer.service.ImageService
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.RedisClient
import com.software.mymusicplayer.utils.RetrofitClient
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.junit.Test

import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import java.io.File
import java.util.UUID
import kotlin.enums.enumEntries
import kotlin.random.Random

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.jamendo.com/v3.0/tracks/?client_id=d3700a33&format=json")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val okHttpClient = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .url("https://api.jamendo.com/v3.0/tracks/?client_id=d3700a33&format=json&search=rock")
            .get()
            .build()
    var f = false
       RetrofitClient.getInstance().getService(BaseBeanService::class.java).getBaseBean("rock",1000)
            .enqueue(object : Callback<BaseBean> {
                override fun onResponse(p0: Call<BaseBean>, p1: Response<BaseBean>) {
                    println(p1.code().toString())
                    if (p1.code() == 200) {
                        val results = p1.body()?.results!!
                        println(results.toString())
                        val songs:MutableList<Song> = mutableListOf()
                        val artists:MutableSet<Artist> = mutableSetOf()
                        val albums:MutableSet<Album> = mutableSetOf()
                        for (result in results) {
                            val album = Album(result.albumName,result.albumId,result.releaseDate,result.albumImage)
                            val artist = Artist(result.artistId,result.artistName,result.artistIdStr)
                            val song = Song(result.songId,result.name,
                                result.duration,artist,album,result.licenseCcUrl,
                                result.position,result.audio,result.audioDownload,result.proUrl,
                                result.shortUrl,result.shareUrl,result.waveform,
                                result.image,result.audioDownloadAllowed)
                            albums.add(album)
                            artists.add(artist)
                            songs.add(song)
                        }

                    }
                }

                override fun onFailure(p0: Call<BaseBean>, p1: Throwable) {
                }
            })
        while (true){}

    }
    @Test
    fun test1(){
        val service = RedisClient.getInstance().getService(RedisService::class.java)
        val execute = service.get("name").execute()
        println(execute.body()?.value)


    }
    @Test
    fun test2(){
        val service = RedisClient.getInstance().getService(RedisService::class.java)
        val execute = service.setEx("a", "80",-1).execute()
        println(execute.body()?.status)
    }
    @Test
    fun test3(){
        val service = RedisClient.getInstance().getService(RedisService::class.java)
        val execute = service.sCard("post:123:liked_users").execute()
        println(execute.body()?.value)

    }
    @Test
    fun test4(){
        val service = RedisClient.getInstance().getService(RedisService::class.java)
        val execute = service.sAdd("uu").execute()
        println(execute.body()?.value)

    }
    @Test
    fun test5(){
        val service = RedisClient.getInstance().getService(RedisService::class.java)
        val execute = service.zAdd("user:${UserDatabaseManager.user?.userName}:playlist", mapOf(Pair<String,Double>("a",1.1),Pair<String,Double>("b",1.2))).execute()
        println(execute.body()?.status)

    }
    @Test
    fun test6(){
        val service = RedisClient.getInstance().getService(RedisService::class.java)
        val execute = service.del("ss").execute()
        println(execute.body()?.value)

    }

    @Test
    fun test7(){
        val service = RedisClient.getInstance().getService(RedisService::class.java)
        val execute = service.zRange("user:rootuu:playlist",0,-1).execute()
        println(execute.body())

    }
    @Test
    fun test8(){
        val service = RedisClient.getInstance().getService(RedisService::class.java)
        val execute = service.zRevRange("user:rootuu:playlist",0,-1).execute()
        println(execute.body())

    }
    @Test
    fun test9(){
        val service = RedisClient.getInstance().getService(RedisService::class.java)
        val value= service.zIncBy(
            "user:null:playlist", 1.0,
            members = "a",
        ).execute()
        println(value.body()?.value)

    }
    @Test
    fun test10(){
        // 获取文件对象
        val file = File("D:\\image\\7.png")

// 创建 RequestBody（包含 MIME 类型）
        val requestFile = file.asRequestBody("image/png".toMediaType())

// 构建 MultipartBody.Part
        val part = MultipartBody.Part.createFormData(
            "image",         // 字段名（需与后端接口一致）
            UUID.randomUUID().toString() + file.name,       // 文件名（可自定义）
            requestFile      // 文件内容 + MIME 类型
        )
        val service =
            RedisClient.getInstance()
                .getService(ImageService::class.java)
                .uploadImage(part)
                .execute()
        println(service.body()?.url)

    }
    @Test
    fun test11(){
//        /images/baf80bf4-57d2-4d78-99c1-099412c6cfa77.png
        val service =
            RedisClient.getInstance()
                .getService(ImageService::class.java)
                .getImageInfo("baf80bf4-57d2-4d78-99c1-099412c6cfa77.png")
                .execute()
        println(service.body()?.mimeType)

    }
    @Test
    fun test12(){
        val service =
            RedisClient.getInstance()
                .getService(RedisService::class.java)
                .sIsMember("uu","c")
                .execute()
        println(service.body()?.value.toBoolean())
    }
    @Test
    fun test13(){
        val service =
            RedisClient.getInstance()
                .getService(RedisService::class.java)
                .sRem("uu","a")
                .execute()
        println(service.body()?.value)
    }



}