package com.software.mymusicplayer.manager

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.google.gson.Gson
import com.software.mymusicplayer.PlaySongFragment
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.RedisClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object PlaySongFragmentManager {
    val playSongFragmentManager = mutableMapOf<String,PlaySongFragment>()
    var currentProgress = 0
    var mHandler = Handler(Looper.getMainLooper()){
        val progress = it.arg1
        progressListeners.forEach {
            if (!it.getDragging()){
                currentProgress=progress
                it.updateUI(currentProgress)
            }
        }
        true
    }
    var alPlaySongs = mutableListOf<Song>()
    private val progressListeners = mutableListOf<ProgressListener>()
    fun registerProgressListener(progressListener: ProgressListener){
        progressListeners.add(progressListener)
    }
    interface ProgressListener{
        fun getDragging():Boolean
        fun updateUI(progress:Int)

    }
    fun getData(){
        RedisClient.getInstance()
            .getService(RedisService::class.java)
            .zRevRange("user:${UserDatabaseManager.user?.userName}:playlist",0,-1)
            .enqueue(object : Callback<Set<String>>{
                override fun onResponse(p0: Call<Set<String>>, p1: Response<Set<String>>) {
                    Log.i("p1_zrevrange",p1.body().toString())
                    val songs = mutableSetOf<Song>()
                    p1.body()?.forEach {
                        s: String ->
                        Log.i("songS",s)
                        val song = Gson().fromJson(s,Song::class.java)
                        songs.add(song)
                    }
                    alPlaySongs.addAll(songs)
                }

                override fun onFailure(p0: Call<Set<String>>, p1: Throwable) {
                    Log.i("p1_zrevrange1",p1.message.toString())
                }

            })
    }
}