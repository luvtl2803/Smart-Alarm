package com.anhq.smartalarm.core.data.repository

import com.anhq.smartalarm.core.database.dao.AlarmDao
import com.anhq.smartalarm.core.model.Alarm
import com.anhq.smartalarm.core.model.mapToAlarm
import com.anhq.smartalarm.core.model.mapToAlarms
import com.anhq.smartalarm.core.model.toAlarmEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultAlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao
) : AlarmRepository {
    override suspend fun insertAlarm(alarm: Alarm): Int {
        return withContext(Dispatchers.IO) {
            alarmDao.insertAlarm(alarm.toAlarmEntity()).toInt()
        }
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        withContext(Dispatchers.IO) {
            alarmDao.updateAlarm(alarm.toAlarmEntity())
        }
    }

    override suspend fun updateAlarmStatus(alarmId: Int, isEnable: Boolean) {
        withContext(Dispatchers.IO) {
            alarmDao.updateAlarmStatus(alarmId, isEnable)
        }
    }

    override fun getAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAlarms().map { it.mapToAlarms() }
    }

    override fun getAlarmById(alarmId: Int): Flow<Alarm> {
        return alarmDao.getAlarmById(alarmId).map { it.mapToAlarm() }
    }
}