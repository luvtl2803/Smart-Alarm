package com.anhq.smartalarm.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "alarm_entity")
@TypeConverters(Converters::class)
data class AlarmEntity(
    @PrimaryKey val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val repeatDays: List<Int>,
    val label: String,
    val isEnabled: Boolean,
    val isVibrate: Boolean,
    val timeInMillis: Long
)



