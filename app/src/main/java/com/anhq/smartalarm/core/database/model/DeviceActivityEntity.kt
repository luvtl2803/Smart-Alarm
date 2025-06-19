package com.anhq.smartalarm.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_activity")
data class DeviceActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val isActive: Boolean, // true = device active, false = inactive/sleep
    val dayOfWeek: Int // 1-7 for Monday-Sunday
) 