package com.software.mymusicplayer.entity
import com.software.mymusicplayer.entity.SongList.Album
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Song(
    val songId:String,val name:String,val duration: Int,
    val artist: Artist,
    val album: Album,
    val licenseCcUrl:String,
    val position:Int,
    val audio:String,
    val audioDownload:String,
    val proUrl:String,
    val shortUrl:String,
    val shareUrl:String,
    val waveform:String,
    val image:String,
    val audioDownloadAllowed:Boolean
) : Parcelable {
    constructor():this("","",0,Artist(),Album(),"",
        0,"","",
        "","","","","",false)
   constructor(songId:String,title:String, duration:Int,image:String,audio: String):this(songId,title,duration,Artist(),Album(),"",
       0,audio,audio,
       "","","","",image,true)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Song

        if (songId != other.songId) return false
        if (name != other.name) return false
        if (duration != other.duration) return false
        if (artist != other.artist) return false
        if (album != other.album) return false
        if (licenseCcUrl != other.licenseCcUrl) return false
        if (position != other.position) return false
        if (audio != other.audio) return false
        if (audioDownload != other.audioDownload) return false
        if (proUrl != other.proUrl) return false
        if (shortUrl != other.shortUrl) return false
        if (shareUrl != other.shareUrl) return false
        if (waveform != other.waveform) return false
        if (image != other.image) return false
        if (audioDownloadAllowed != other.audioDownloadAllowed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = songId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + duration
        result = 31 * result + artist.hashCode()
        result = 31 * result + album.hashCode()
        result = 31 * result + licenseCcUrl.hashCode()
        result = 31 * result + position
        result = 31 * result + audio.hashCode()
        result = 31 * result + audioDownload.hashCode()
        result = 31 * result + proUrl.hashCode()
        result = 31 * result + shortUrl.hashCode()
        result = 31 * result + shareUrl.hashCode()
        result = 31 * result + waveform.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + audioDownloadAllowed.hashCode()
        return result
    }

}
