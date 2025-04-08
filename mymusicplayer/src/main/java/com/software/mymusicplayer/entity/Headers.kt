package com.software.mymusicplayer.entity

import com.google.gson.annotations.SerializedName

data class Headers(
    val status:String,val code:Int,
    @SerializedName("error_message") val errorMessage:String,
    val warnings:String,
    @SerializedName("results_count") val resultsCount:Int,
    val next:String
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
