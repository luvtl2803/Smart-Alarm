package com.anhq.smartalarm.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timers")
data class TimerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val initialTimeMillis: Long,
    val currentInitialTimeMillis: Long,
    val remainingTimeMillis: Long,
    val lastTickTime: Long,
    val isRunning: Boolean,
    val isPaused: Boolean,
    val endedAt: Long? = null
) 