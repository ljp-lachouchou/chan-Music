package com.software.mymusicplayer.entity

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("value")
    val value:T
)