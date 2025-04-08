package com.software.mymusicplayer.entity

import com.software.mymusicplayer.enums.IdentityType

data class UserAuth(
    val id:Long,
    val user_id:Long,
    val identityType:Enum<IdentityType>,
    val identifier:String, //登录标识
    val credential:String, //唯一凭证
    val verified:Short //是否已验证
)