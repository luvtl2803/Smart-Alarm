package com.anhq.smartalarm.core.database.converter

import androidx.room.TypeConverter
import com.anhq.smartalarm.core.model.AlarmGameType
import javax.inject.Inject

class AlarmGameTypeConverter @Inject constructor() {
    @TypeConverter
    fun toGameType(value: Int): AlarmGameType = AlarmGameType.entries[value]

    @TypeConverter
    fun fromGameType(gameType: AlarmGameType): Int = gameType.ordinal
} 