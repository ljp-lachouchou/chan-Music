package com.software.mymusicplayer.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.software.mymusicplayer.entity.Note
import kotlinx.coroutines.flow.Flow


@Dao
interface NoteDao {
    @Insert
    suspend fun insertNote(note: Note):Long
    @Query("SELECT * FROM note WHERE id = :noteId")
    fun getNoteById(noteId:Long):Note?
    @Query("SELECT * FROM note WHERE user_id = :userId ORDER BY publish_time DESC")
    suspend fun getNotesByUser(userId: String): List<Note>
    @Query("SELECT * FROM note LIMIT :limit OFFSET :pages;")
    suspend fun getNotesByLimit(limit:Int,pages:Int):List<Note>
    @Query("SELECT * FROM note LIMIT :limit")
    suspend fun getNotes(limit:Int):List<Note>
    @Query("DELETE FROM note")
    fun deleteAll()
}