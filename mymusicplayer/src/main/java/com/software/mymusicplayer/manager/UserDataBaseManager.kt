package com.software.mymusicplayer.manager

import android.content.Context
import androidx.room.Room
import com.software.mymusicplayer.databases.UserDataBase
import com.software.mymusicplayer.entity.User

class UserDatabaseManager private constructor(context: Context) {
    private val db: UserDataBase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            UserDataBase::class.java,
            "user_database.db"
        ).build()
    }

    companion object {
        var user:User? = null
        @Volatile
        private var instance: UserDatabaseManager? = null
        fun getInstance(context: Context): UserDatabaseManager =
            instance ?: synchronized(this) {
                instance ?: UserDatabaseManager(context).also { instance = it }
            }
    }

    fun getDatabase(): UserDataBase = db
}