package com.software.mymusicplayer.entity

import com.google.gson.annotations.SerializedName
import kotlin.time.Duration

data class Result(
    @SerializedName("id") val songId:String,val name:String,val duration: Int,
    @SerializedName("artist_id") val artistId:String,
    @SerializedName("artist_name") val artistName:String,
    @SerializedName("artist_idstr") val artistIdStr:String,
    @SerializedName("album_name") val albumName:String,
    @SerializedName("album_id") val albumId:String,
    @SerializedName("license_ccurl") val licenseCcUrl:String,
    val position:Int,
    @SerializedName("releasedate") val releaseDate:String,
    @SerializedName("album_image") val albumImage:String,
    val audio:String,
    @SerializedName("audiodownload") val audioDownload:String,
    @SerializedName("prourl") val proUrl:String,
    @SerializedName("shorturl") val shortUrl:String,
    @SerializedName("shareurl") val shareUrl:String,
    val waveform:String,
    val image:String,
    @SerializedName("audiodownload_allowed") val audioDownloadAllowed:Boolean
)
/**
 * Copyright 2025 bejson.com
 */



/**
 * Auto-generated: 2025-03-01 17:40:15
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */

