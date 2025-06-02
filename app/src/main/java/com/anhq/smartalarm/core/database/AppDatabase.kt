package com.anhq.smartalarm.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.anhq.smartalarm.core.database.converter.AlarmGameTypeConverter
import com.anhq.smartalarm.core.database.converter.DayOfWeekSetConverter
import com.anhq.smartalarm.core.database.dao.AlarmDao
import com.anhq.smartalarm.core.database.model.AlarmEntity

@Database(
    entities = [AlarmEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(value = [DayOfWeekSetConverter::class, AlarmGameTypeConverter::class])
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
} 