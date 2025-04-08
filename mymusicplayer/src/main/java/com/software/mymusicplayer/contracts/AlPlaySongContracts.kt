package com.software.mymusicplayer.contracts

import com.software.mymusicplayer.entity.Song
import retrofit2.Call


interface AlPlaySongContracts {
    interface presenter{
        fun managerData()
    }
    interface iModel{
        fun getData():Call<Set<String>>
    }
    interface iView{
        fun successTODO(songs:MutableSet<Song>)
        fun finalTODO(t:Throwable?)
    }
}