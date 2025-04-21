package com.anhq.smartalarm.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.anhq.smartalarm.core.database.dao.AlarmDao
import com.anhq.smartalarm.core.database.model.AlarmEntity
import com.anhq.smartalarm.core.database.model.Converters

@Database(
    entities = [
        AlarmEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}