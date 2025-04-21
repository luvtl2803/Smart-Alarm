package com.anhq.smartalarm.core.data.repository

import com.anhq.smartalarm.core.model.Alarm

interface AlarmRepository {
    suspend fun insertAlarm(alarm: Alarm)
}