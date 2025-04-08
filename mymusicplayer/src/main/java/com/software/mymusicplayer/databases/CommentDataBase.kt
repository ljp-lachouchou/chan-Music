package com.software.mymusicplayer.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.software.mymusicplayer.dao.CommentDao
import com.software.mymusicplayer.entity.SongComment
import com.software.mymusicplayer.entity.Replay

@Database(
entities = [SongComment::class,Replay::class],
version = 1,
exportSchema = false  // 若需迁移需设为 true
)
abstract class CommentDataBase : RoomDatabase() {
    abstract fun getCommentDao():CommentDao
}