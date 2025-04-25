package com.anhq.smartalarm.core.data.repository

import com.anhq.smartalarm.core.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    suspend fun insertAlarm(alarm: Alarm) : Int
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun updateAlarmStatus(alarmId: Int, isEnable: Boolean)
    fun getAlarmById(alarmId: Int): Flow<Alarm>
    fun getAlarms() : Flow<List<Alarm>>
}