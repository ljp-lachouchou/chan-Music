package com.software.mymusicplayer.model

import com.software.mymusicplayer.contracts.RecommendContracts
import com.software.mymusicplayer.entity.BaseBean
import com.software.mymusicplayer.service.BaseBeanService
import com.software.mymusicplayer.utils.RetrofitClient
import retrofit2.Call
import kotlin.random.Random

class RecommendModel:RecommendContracts.IModel {

    companion object{
        val SEARCH_STRING = listOf("Rock","Hip-Hop","R&B","Pop","Jazz")
        var SEARCH_VALUE1 =  Random.nextInt(0,4)
        var OFFSET_VALUE1 = Random.nextInt(0,10000)
        var SEARCH_VALUE2 =  Random.nextInt(0,4)
        var OFFSET_VALUE2 = Random.nextInt(0,10000)
    }
    override fun getDataOne(): Call<BaseBean> {
        val bbs = RetrofitClient.getInstance().getService(BaseBeanService::class.java)
        return bbs.getBaseBean(SEARCH_STRING[SEARCH_VALUE1],OFFSET_VALUE1)
    }

    override fun getDataTwo(): Call<BaseBean> {
        val bbs = RetrofitClient.getInstance().getService(BaseBeanService::class.java)
        return bbs.getBaseBean(SEARCH_STRING[SEARCH_VALUE2],OFFSET_VALUE2)
    }


}