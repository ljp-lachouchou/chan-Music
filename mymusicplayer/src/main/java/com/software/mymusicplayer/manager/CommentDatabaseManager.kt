package com.software.mymusicplayer.manager

import android.content.Context
import androidx.room.Room
import com.software.mymusicplayer.databases.CommentDataBase
import com.software.mymusicplayer.databases.NoteDataBase

class CommentDatabaseManager private constructor(context: Context) {
    private val db: CommentDataBase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            CommentDataBase::class.java,
            "comment_database.db"
        ).build()
    }

    companion object {
        @Volatile
        private var instance: CommentDatabaseManager? = null
        fun getInstance(context: Context): CommentDatabaseManager =
            instance ?: synchronized(this) {
                instance ?: CommentDatabaseManager(context).also { instance = it }
            }
    }

    fun getDatabase(): CommentDataBase = db
}