package com.anhq.smartalarm.core.data.repository

import com.anhq.smartalarm.core.database.dao.AlarmHistoryDao
import com.anhq.smartalarm.core.database.model.AlarmHistoryEntity
import com.anhq.smartalarm.core.model.DayOfWeek
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class AlarmHistoryRepository @Inject constructor(
    private val alarmHistoryDao: AlarmHistoryDao
) {
    fun getAlarmHistory(alarmId: Int): Flow<List<AlarmHistoryEntity>> =
        alarmHistoryDao.getAlarmHistory(alarmId)

    fun getAlarmHistoryByDay(dayOfWeek: DayOfWeek): Flow<List<AlarmHistoryEntity>> =
        alarmHistoryDao.getAlarmHistoryByDay(dayOfWeek)

    fun getRecentHistory(limit: Int = 50): Flow<List<AlarmHistoryEntity>> =
        alarmHistoryDao.getRecentHistory(limit)

    suspend fun recordAlarmHistory(
        alarmId: Int,
        userAction: String,
        triggeredAt: Long = System.currentTimeMillis()
    ) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = triggeredAt
        }
        
        // Chuyển đổi từ Calendar.DAY_OF_WEEK sang DayOfWeek của chúng ta
        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> DayOfWeek.MON
            Calendar.TUESDAY -> DayOfWeek.TUE
            Calendar.WEDNESDAY -> DayOfWeek.WED
            Calendar.THURSDAY -> DayOfWeek.THU
            Calendar.FRIDAY -> DayOfWeek.FRI
            Calendar.SATURDAY -> DayOfWeek.SAT
            Calendar.SUNDAY -> DayOfWeek.SUN
            else -> DayOfWeek.MON // Fallback, không nên xảy ra
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

    suspend fun getAlarmTriggerCount(alarmId: Int): Int =
        alarmHistoryDao.getAlarmTriggerCount(alarmId)

    suspend fun getAverageTimeToDisable(alarmId: Int): Long? =
        alarmHistoryDao.getAverageTimeToDisable(alarmId)

    suspend fun cleanupOldHistory(olderThan: Long = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) {
        alarmHistoryDao.deleteHistoryOlderThan(olderThan)
    }
} 