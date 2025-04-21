package com.anhq.smartalarm.core.database.model

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return Json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        return Json.decodeFromString(value)
    }
}