package com.software.mymusicplayer.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "replies",
    foreignKeys = [
        ForeignKey(
            entity = SongComment::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ] ,
    indices = [Index("parentId")]
)
data class Replay(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val content: String,
    val userId: String, // 发布者ID（非空）
    val parentId: Long// 外键关联父评论
)
