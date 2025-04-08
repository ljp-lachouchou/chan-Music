package com.software.mymusicplayer.contracts

import com.software.mymusicplayer.entity.SongList.Album
import com.software.mymusicplayer.entity.Artist
import com.software.mymusicplayer.entity.BaseBean
import com.software.mymusicplayer.entity.Song
import retrofit2.Call

interface RecommendContracts {
    interface IPresenter{
        /**
         * 处理第一个推荐的数据
         */
        fun getDataOne()
        /**
         * 处理第二个推荐的数据
         */
        fun getDataTwo()
    }
    interface IModel{
        /**
         * 获得数据
         */
        fun getDataOne(): Call<BaseBean>
        /**
         * 获得数据
         */
        fun getDataTwo():Call<BaseBean>

    }
    interface IView{
        fun successOneTODO(songs: MutableList<Song>,
                           artists: MutableSet<Artist>,albums: MutableSet<Album>)
        fun failOneTODO(t:Throwable?)
        fun successTwoTODO(songs: MutableList<Song>,
            artists: MutableSet<Artist>,albums: MutableSet<Album>)//songs: MutableList<Song>,
        //artists: MutableSet<Artist>,albums: MutableSet<Album>
        fun failTwoTODO(t:Throwable?)
    }
}