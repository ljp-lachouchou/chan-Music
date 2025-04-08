package com.software.mymusicplayer.model

import com.software.mymusicplayer.contracts.AlbumContracts
import com.software.mymusicplayer.entity.BaseBean
import com.software.mymusicplayer.service.BaseBeanService
import com.software.mymusicplayer.utils.RetrofitClient
import retrofit2.Call

class AlbumModel : AlbumContracts.IModel {
    override fun getData(albumId:String):Call<BaseBean> {
        val service = RetrofitClient.getInstance().getService(BaseBeanService::class.java)
        return service.getBaseBeanByAlbumId(albumId)
    }

}