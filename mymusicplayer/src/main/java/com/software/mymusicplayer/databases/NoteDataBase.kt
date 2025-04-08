package com.software.mymusicplayer.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.software.mymusicplayer.converter.JsonConverter
import com.software.mymusicplayer.converter.TimeConverter
import com.software.mymusicplayer.dao.NoteDao
import com.software.mymusicplayer.entity.Note

@Database(version = 1, exportSchema = false, entities = [Note::class])
@TypeConverters(value = [TimeConverter::class,JsonConverter::class])
abstract class NoteDataBase : RoomDatabase() {
    abstract fun  getDao():NoteDao

}