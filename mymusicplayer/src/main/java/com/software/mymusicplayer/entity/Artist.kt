package com.software.mymusicplayer.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artist(@SerializedName("artist_id") val artistId:String,
                  @SerializedName("artist_name") val artistName:String,
                  @SerializedName("artist_idstr") val artistIdStr:String):Parcelable{
    constructor():this("","","")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Artist

        if (artistId != other.artistId) return false
        if (artistName != other.artistName) return false
        if (artistIdStr != other.artistIdStr) return false

        return true
    }

    override fun hashCode(): Int {
        var result = artistId.hashCode()
        result = 31 * result + artistName.hashCode()
        result = 31 * result + artistIdStr.hashCode()
        return result
    }

}
