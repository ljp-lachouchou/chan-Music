package com.software.mymusicplayer.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "users",
    indices = [Index(value = ["user_name"], unique = true)]
    )

data class User(
    @PrimaryKey(autoGenerate = true)
    val id:Long = 0,
    @ColumnInfo(name = "user_name")
    var userName:String,
    @ColumnInfo(name = "hash_password")
    var pwd:String,
    @ColumnInfo(name = "nick_name")
    var nickName:String=UUID.randomUUID().toString(),
    @ColumnInfo(name = "avatar")
    var avatar:String?,
    @ColumnInfo(name = "created_at")
    var createdAt:Timestamp,
    @ColumnInfo(name = "updated_at")
    var updatedAt:Timestamp
    ){
    override fun toString(): String {
        return "User(id=$id, userName='$userName', pwd='$pwd', nickName='$nickName', avatar=$avatar, createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}