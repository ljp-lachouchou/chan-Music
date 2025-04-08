package com.software.mymusicplayer.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.software.mymusicplayer.dao.UserDao
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.converter.TimeConverter

@Database(version = 1, entities = arrayOf(User::class), exportSchema = false)
@TypeConverters(TimeConverter::class)
abstract class UserDataBase : RoomDatabase() {
    abstract fun getUserDao():UserDao

}