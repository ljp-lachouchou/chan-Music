package com.software.mymusicplayer.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.sql.Timestamp

@Entity(
    tableName = "note",
    indices = [
        Index(value = ["user_id"], name = "idx_user"),
        Index(value = ["publish_time"], name = "idx_time")
    ]
)
@Parcelize
data class Note(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "title")
    val title: String?=null,

    @ColumnInfo(name = "content")
    val content: String?=null,

    @ColumnInfo(name = "images")
    val images: List<String>,

    @ColumnInfo(name = "song_url")
    val songUrl: String? = null,

    @ColumnInfo(name = "publish_time")
    val publishTime: Timestamp
):Parcelable{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Note

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (title != other.title) return false
        if (content != other.content) return false
        if (images != other.images) return false
        if (songUrl != other.songUrl) return false
        if (publishTime != other.publishTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + images.hashCode()
        result = 31 * result + (songUrl?.hashCode() ?: 0)
        result = 31 * result + publishTime.hashCode()
        return result
    }
}