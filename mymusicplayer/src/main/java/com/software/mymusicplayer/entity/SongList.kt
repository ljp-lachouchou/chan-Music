package com.software.mymusicplayer.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

abstract class SongList{

    @Parcelize
    data class Album(val albumName:String,
                     val albumId:String,
                     val releaseDate:String,
                     val albumImage:String): Parcelable {
        constructor():this("","","","")

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Album

            if (albumName != other.albumName) return false
            if (albumId != other.albumId) return false
            if (releaseDate != other.releaseDate) return false
            if (albumImage != other.albumImage) return false

            return true
        }

        override fun hashCode(): Int {
            var result = albumName.hashCode()
            result = 31 * result + albumId.hashCode()
            result = 31 * result + releaseDate.hashCode()
            result = 31 * result + albumImage.hashCode()
            return result
        }

    }
    @Parcelize
    data class PlayList(val playListName:String,
                     val playListNameId:String,
                     val releaseDate:String,
                     val playListImage:String,
        val userId:String): Parcelable {
        constructor():this("","","","","")

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PlayList

            if (playListName != other.playListName) return false
            if (playListNameId != other.playListNameId) return false
            if (releaseDate != other.releaseDate) return false
            if (playListImage != other.playListImage) return false
            if (userId != other.userId) return false

            return true
        }

        override fun hashCode(): Int {
            var result = playListName.hashCode()
            result = 31 * result + playListNameId.hashCode()
            result = 31 * result + releaseDate.hashCode()
            result = 31 * result + playListImage.hashCode()
            result = 31 * result + userId.hashCode()
            return result
        }


    }
}