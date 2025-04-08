package com.software.mymusicplayer.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.software.mymusicplayer.entity.SongComment
import com.software.mymusicplayer.entity.Replay

@Dao
interface CommentDao {
    @Insert
    suspend fun insertSongComment(comment: SongComment): Long

    @Insert
    fun insertReply(reply: Replay): Long

    @Query("DELETE FROM comments WHERE id = :id")
    fun deleteByCommentId(id:Long)
    @Query("SELECT * FROM replies WHERE parentId = :parentId")
    fun queryReplayByParentId(parentId:Long):List<Replay>
    @Query("SELECT COUNT(1) FROM replies WHERE parentId = :parentId")
    fun queryReplayCountByParentId(parentId:Long):Long
    @Query("SELECT * FROM comments WHERE thId = :thId")
    fun queryCommentsByThId(thId:String):List<SongComment>
}