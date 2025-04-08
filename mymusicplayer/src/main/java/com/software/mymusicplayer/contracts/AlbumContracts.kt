package com.software.mymusicplayer.contracts

import com.software.mymusicplayer.entity.BaseBean
import com.software.mymusicplayer.entity.Song
import retrofit2.Call

interface AlbumContracts {
    interface IPresenter{
        fun managerAlbumData()
    }
    interface IView{
        fun successTODO(songs:MutableList<Song>)
        fun failTODO(e:Throwable)
    }
    interface IModel{
        fun getData(albumId:String):Call<BaseBean>
    }
}