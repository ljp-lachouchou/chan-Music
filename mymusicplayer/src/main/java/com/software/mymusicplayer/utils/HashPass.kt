package com.software.mymusicplayer.utils

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object HashPass {
    fun hashSHA(input:String):String{
        val md = MessageDigest.getInstance("SHA-256")
        val hashByte = md.digest(input.toByteArray(StandardCharsets.UTF_8))
        val hexString = StringBuilder()
        for (b in hashByte){
            hexString.append(String.format("%02x",b))
        }
        return hexString.toString()

    }
}