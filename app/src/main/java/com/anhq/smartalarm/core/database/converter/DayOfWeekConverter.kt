package com.anhq.smartalarm.core.database.converter

import androidx.room.TypeConverter
import java.time.DayOfWeek

class DayOfWeekConverter {
    @TypeConverter
    fun toDayOfWeek(value: Int): DayOfWeek = DayOfWeek.of(value)

    @TypeConverter
    fun fromDayOfWeek(day: DayOfWeek): Int = day.value
} 