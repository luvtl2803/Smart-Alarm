package com.anhq.smartalarm.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.core.model.DayOfWeek

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val isActive: Boolean = true,
    val isVibrate: Boolean = true,
    val selectedDays: Set<DayOfWeek> = emptySet(),
    val label: String = "",
    val gameType: AlarmGameType = AlarmGameType.NONE,
    val soundUri: String = ""
)