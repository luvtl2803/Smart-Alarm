package com.anhq.smartalarm.core.data.repository

import com.anhq.smartalarm.core.database.dao.AlarmHistoryDao
import com.anhq.smartalarm.core.database.model.AlarmHistoryEntity
import com.anhq.smartalarm.core.model.DayOfWeek
import java.util.Calendar
import javax.inject.Inject

class AlarmHistoryRepository @Inject constructor(
    private val alarmHistoryDao: AlarmHistoryDao
) {
    suspend fun recordAlarmHistory(
        alarmId: Int,
        userAction: String,
        triggeredAt: Long = System.currentTimeMillis()
    ) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = triggeredAt
        }

        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> DayOfWeek.MON
            Calendar.TUESDAY -> DayOfWeek.TUE
            Calendar.WEDNESDAY -> DayOfWeek.WED
            Calendar.THURSDAY -> DayOfWeek.THU
            Calendar.FRIDAY -> DayOfWeek.FRI
            Calendar.SATURDAY -> DayOfWeek.SAT
            Calendar.SUNDAY -> DayOfWeek.SUN
            else -> DayOfWeek.MON
        }

        val history = AlarmHistoryEntity(
            alarmId = alarmId,
            triggeredAt = triggeredAt,
            userAction = userAction,
            actionTime = System.currentTimeMillis(),
            dayOfWeek = dayOfWeek
        )
        alarmHistoryDao.insertHistory(history)
    }
} 