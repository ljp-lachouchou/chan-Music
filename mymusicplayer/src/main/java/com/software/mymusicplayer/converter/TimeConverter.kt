package com.software.mymusicplayer.converter

import androidx.room.TypeConverter
import java.sql.Timestamp

class TimeConverter {
    @TypeConverter
    fun timestampToLong(timestamp: Timestamp):Long = timestamp.time
    @TypeConverter
    fun longToTimestamp(long: Long):Timestamp = Timestamp(long)
}