package com.software.mymusicplayer.manager

import android.content.Context
import androidx.room.Room
import com.software.mymusicplayer.databases.NoteDataBase

class NoteDatabaseManager private constructor(context: Context) {
    private val db: NoteDataBase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            NoteDataBase::class.java,
            "note_database.db"
        ).build()
    }

    companion object {
        @Volatile
        private var instance: NoteDatabaseManager? = null
        fun getInstance(context: Context): NoteDatabaseManager =
            instance ?: synchronized(this) {
                instance ?: NoteDatabaseManager(context).also { instance = it }
            }
    }

    fun getDatabase(): NoteDataBase = db
}