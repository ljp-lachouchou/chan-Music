package com.software.mymusicplayer.model

import com.software.mymusicplayer.contracts.AlPlaySongContracts
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.RedisClient
import retrofit2.Call

class AlPlaySongModel : AlPlaySongContracts.iModel {
    private var user: User? = null
    override fun getData(): Call<Set<String>> {
        user = UserDatabaseManager.user
        return RedisClient.getInstance()
            .getService(RedisService::class.java)
            .zRevRange("user:${user?.userName}:playlist",0,-1)
    }
}