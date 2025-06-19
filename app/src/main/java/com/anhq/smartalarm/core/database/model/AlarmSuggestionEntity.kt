package com.anhq.smartalarm.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anhq.smartalarm.core.model.DayOfWeek

@Entity(tableName = "alarm_suggestions")
data class AlarmSuggestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val dayOfWeek: DayOfWeek,
    val confidence: Float, // 0-1 indicating confidence level
    val lastUpdated: Long,
    val suggestedCount: Int = 0, // How many times this suggestion was made
    val acceptedCount: Int = 0   // How many times user accepted this suggestion
) 