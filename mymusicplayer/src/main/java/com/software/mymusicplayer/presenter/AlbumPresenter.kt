package com.software.mymusicplayer.presenter

import com.software.mymusicplayer.contracts.AlbumContracts
import com.software.mymusicplayer.entity.Artist
import com.software.mymusicplayer.entity.BaseBean
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.entity.SongList.Album
import com.software.mymusicplayer.model.AlbumModel
import retrofit2.Call
import retrofit2.Response

class AlbumPresenter(val view:AlbumContracts.IView,val albumId: String) : AlbumContracts.IPresenter {
    private var model: AlbumContracts.IModel
    init {
        model = AlbumModel()
    }
    override fun managerAlbumData() {
        val songs:MutableList<Song> = mutableListOf()
        model.getData(albumId).enqueue(object : retrofit2.Callback<BaseBean> {
            override fun onResponse(p0: Call<BaseBean>, p1: Response<BaseBean>) {
                for(result in p1.body()?.results!!){
                    val album = Album(result.albumName,result.albumId,result.releaseDate,result.albumImage)
                    val artist = Artist(result.artistId,result.artistName,result.artistIdStr)
                    val song = Song(result.songId,result.name,
                        result.duration,artist,album,result.licenseCcUrl,
                        result.position,result.audio,result.audioDownload,result.proUrl,
                        result.shortUrl,result.shareUrl,result.waveform,
                        result.image,result.audioDownloadAllowed)
                    songs.add(song)
                }
                view.successTODO(songs)
            }

            override fun onFailure(p0: Call<BaseBean>, p1: Throwable) {
                view.failTODO(p1)
            }

        })
    }
}