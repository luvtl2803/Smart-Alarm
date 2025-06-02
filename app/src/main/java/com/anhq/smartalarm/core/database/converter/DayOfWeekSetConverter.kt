package com.anhq.smartalarm.core.database.converter

import androidx.room.TypeConverter
import com.anhq.smartalarm.core.model.DayOfWeek
import javax.inject.Inject

class DayOfWeekSetConverter @Inject constructor() {
    @TypeConverter
    fun toDayOfWeekSet(value: String): Set<DayOfWeek> {
        if (value.isEmpty()) return emptySet()
        return value.split(",").map { DayOfWeek.valueOf(it) }.toSet()
    }

    @TypeConverter
    fun fromDayOfWeekSet(days: Set<DayOfWeek>): String {
        return days.joinToString(",") { it.name }
    }
} 