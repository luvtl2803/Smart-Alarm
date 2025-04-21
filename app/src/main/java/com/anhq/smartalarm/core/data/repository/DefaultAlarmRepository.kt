package com.anhq.smartalarm.core.data.repository

import com.anhq.smartalarm.core.database.dao.AlarmDao
import com.anhq.smartalarm.core.model.Alarm
import com.anhq.smartalarm.core.model.toAlarmEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultAlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao
) : AlarmRepository {
    override suspend fun insertAlarm(alarm: Alarm) {
        withContext(Dispatchers.IO) {
            alarmDao.insertAlarm(alarm.toAlarmEntity())
        }
    }
}