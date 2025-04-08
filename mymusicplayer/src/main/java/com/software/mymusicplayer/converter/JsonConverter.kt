package com.software.mymusicplayer.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class JsonConverter {
    @TypeConverter
    fun fromJsonToList(json:String):List<String>{
        return json.let {
            Gson().fromJson(it,object : TypeToken<List<String>>(){}.type)
        }
    }
    @TypeConverter
    fun fromListToJson(list: List<String>):String{
        return list.let {
            Gson().toJson(list)
        }
    }
}