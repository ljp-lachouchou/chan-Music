package com.software.mymusicplayer.presenter

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.software.mymusicplayer.contracts.AlPlaySongContracts
import com.software.mymusicplayer.entity.Artist
import com.software.mymusicplayer.entity.BaseBean
import com.software.mymusicplayer.entity.BaseResponse
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.entity.SongList.Album
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.model.AlPlaySongModel
import com.software.mymusicplayer.service.BaseBeanService
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.RedisClient
import com.software.mymusicplayer.utils.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlPlaySongPresenter(val view:AlPlaySongContracts.iView): AlPlaySongContracts.presenter {
    private var model:AlPlaySongContracts.iModel
    private var user:User? = null
    val songs = mutableSetOf<Song>()
    init {
        model = AlPlaySongModel()
    }
    override fun managerData() {
        user = UserDatabaseManager.user
        var count = 0
        RedisClient.getInstance().getService(RedisService::class.java)
            .zCard("user:${user?.userName}:playlist")
            .enqueue(object :Callback<BaseResponse<String>>{
                override fun onResponse(
                    p0: Call<BaseResponse<String>>,
                    p1: Response<BaseResponse<String>>
                ) {
                    count = p1.body()?.value?.toInt()!!
                }

                override fun onFailure(p0: Call<BaseResponse<String>>, p1: Throwable) {

                }

            })
        model.getData()
            .enqueue(object : Callback<Set<String>>{
                override fun onResponse(p0: Call<Set<String>>, p1: Response<Set<String>>) {
                    Log.i("songs_SIZE",songs.size.toString())
                    loadData(p1)
                    Handler(Looper.getMainLooper()).postDelayed({
                        Log.i("songs_PP",songs.size.toString())
                        view.successTODO(songs)
                    },3000)


                }

                override fun onFailure(p0: Call<Set<String>>, p1: Throwable) {
                    view.finalTODO(p1)
                }

            })
    }

    private fun loadData(p1: Response<Set<String>>) {
        p1.body()?.forEach {
            RetrofitClient.getInstance()
                .getService(BaseBeanService::class.java)
                .getBaseBeanBySongId(it).enqueue(object : Callback<BaseBean>{
                    override fun onResponse(
                        p0: Call<BaseBean>,
                        p1: Response<BaseBean>
                    ) {

                        for (result in p1.body()?.results!!){
                            val album = Album(result.albumName,result.albumId,result.releaseDate,result.albumImage)
                            val artist = Artist(result.artistId,result.artistName,result.artistIdStr)
                            val song = Song(result.songId,result.name,
                                result.duration,artist,album,result.licenseCcUrl,
                                result.position,result.audio,result.audioDownload,result.proUrl,
                                result.shortUrl,result.shareUrl,result.waveform,
                                result.image,result.audioDownloadAllowed)
                            Log.i("p1_bySongId",p1.body().toString())
                            songs.add(song)
                        }


                    }

                    override fun onFailure(p0: Call<BaseBean>, p1: Throwable) {
                        Log.i("p1_bySongId1",p1.message.toString())
                    }

                })
        }
    }
}