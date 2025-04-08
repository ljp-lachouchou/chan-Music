package com.software.mymusicplayer.entity

import com.google.gson.annotations.SerializedName

data class BaseStatu<T>(
    @SerializedName("status")
    val status:T
)