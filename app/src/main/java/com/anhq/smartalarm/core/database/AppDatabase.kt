package com.anhq.smartalarm.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.anhq.smartalarm.core.database.converter.AlarmGameTypeConverter
import com.anhq.smartalarm.core.database.converter.DayOfWeekSetConverter
import com.anhq.smartalarm.core.database.dao.AlarmDao
import com.anhq.smartalarm.core.database.dao.AlarmHistoryDao
import com.anhq.smartalarm.core.database.dao.AlarmSuggestionDao
import com.anhq.smartalarm.core.database.dao.DeviceActivityDao
import com.anhq.smartalarm.core.database.dao.TimerDao
import com.anhq.smartalarm.core.database.model.AlarmEntity
import com.anhq.smartalarm.core.database.model.AlarmHistoryEntity
import com.anhq.smartalarm.core.database.model.AlarmSuggestionEntity
import com.anhq.smartalarm.core.database.model.DeviceActivityEntity
import com.anhq.smartalarm.core.database.model.TimerEntity

@Database(
    entities = [
        AlarmEntity::class,
        TimerEntity::class,
        AlarmHistoryEntity::class,
        AlarmSuggestionEntity::class,
        DeviceActivityEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    value = [
        DayOfWeekSetConverter::class,
        AlarmGameTypeConverter::class
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun timerDao(): TimerDao
    abstract fun alarmHistoryDao(): AlarmHistoryDao
    abstract fun alarmSuggestionDao(): AlarmSuggestionDao
    abstract fun deviceActivityDao(): DeviceActivityDao
} 