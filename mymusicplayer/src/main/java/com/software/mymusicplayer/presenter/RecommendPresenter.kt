package com.software.mymusicplayer.presenter

import android.os.Looper
import android.util.Log

import com.software.mymusicplayer.contracts.RecommendContracts
import com.software.mymusicplayer.entity.SongList.Album
import com.software.mymusicplayer.entity.Artist
import com.software.mymusicplayer.entity.BaseBean
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.model.RecommendModel



import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class RecommendPresenter(private var view: RecommendContracts.IView):RecommendContracts.IPresenter {
    private var model:RecommendContracts.IModel
    init {
        model = RecommendModel()
    }
    override fun getDataOne(){

        val songs:MutableList<Song> = mutableListOf()
        val artists:MutableSet<Artist> = mutableSetOf()
        val albums:MutableSet<Album> = mutableSetOf()
//        var f = false
        Log.d("MainThread", "是否为主线程: ${Looper.getMainLooper().thread == Thread.currentThread()}")
        model.getDataOne()
            .enqueue(object :Callback<BaseBean>{
            override fun onResponse(p0: Call<BaseBean>, p1: Response<BaseBean>) {
//                f = true
                val results = p1.body()?.results ?: kotlin.run {
                    Log.e("DATA_ERROR", "results is null")
                    return
                }
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


                Log.d("获取数据",results.toString())

            }
                view.successOneTODO(songs, artists, albums)


        }
            override fun onFailure(p0: Call<BaseBean>, p1: Throwable) {
                view.failOneTODO(p1)
            }



        })
    }

    override fun getDataTwo() {
        model.getDataTwo()
            .enqueue(object :Callback<BaseBean> {

                override fun onResponse(p0: Call<BaseBean>, p1: Response<BaseBean>) {
                    if (p1.code() == 200) {

                        val results = p1.body()?.results!!
                        Log.d("获取数据", results.toString())
                        val songs: MutableList<Song> = mutableListOf()
                        val artists: MutableSet<Artist> = mutableSetOf()
                        val albums: MutableSet<Album> = mutableSetOf()
                        for (result in results) {
                            val album = Album(
                                result.albumName,
                                result.albumId,
                                result.releaseDate,
                                result.albumImage
                            )
                            val artist =
                                Artist(result.artistId, result.artistName, result.artistIdStr)
                            val song = Song(
                                result.songId, result.name,
                                result.duration, artist, album, result.licenseCcUrl,
                                result.position, result.audio, result.audioDownload, result.proUrl,
                                result.shortUrl, result.shareUrl, result.waveform,
                                result.image, result.audioDownloadAllowed
                            )
                            albums.add(album)
                            artists.add(artist)
                            songs.add(song)
                        }

                        view.successTwoTODO(songs, artists, albums)
                    }
                }

                override fun onFailure(p0: Call<BaseBean>, p1: Throwable) {
                    view.failTwoTODO(p1)
                }
            })


    }

}