package com.software.mymusicplayer.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "comments")
data class SongComment(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val content: String,
    val userId: String,
    val thId:String
):Parcelable
