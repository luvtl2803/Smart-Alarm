package com.anhq.smartalarm.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.anhq.smartalarm.core.model.DayOfWeek

@Entity(
    tableName = "alarm_history",
    foreignKeys = [
        ForeignKey(
            entity = AlarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["alarmId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AlarmHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val alarmId: Int,
    val triggeredAt: Long,
    val userAction: String,
    val actionTime: Long,
    val dayOfWeek: DayOfWeek
) 