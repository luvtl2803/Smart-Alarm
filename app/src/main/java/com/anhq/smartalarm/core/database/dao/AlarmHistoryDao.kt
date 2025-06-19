package com.anhq.smartalarm.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.anhq.smartalarm.core.database.model.AlarmHistoryEntity
import com.anhq.smartalarm.core.model.DayOfWeek
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmHistoryDao {
    @Query("SELECT * FROM alarm_history WHERE alarmId = :alarmId ORDER BY triggeredAt DESC")
    fun getAlarmHistory(alarmId: Int): Flow<List<AlarmHistoryEntity>>

    @Query("""
        SELECT * FROM alarm_history 
        WHERE dayOfWeek = :dayOfWeek 
        AND triggeredAt >= :minTimestamp
        ORDER BY triggeredAt DESC
    """)
    fun getAlarmHistoryByDay(dayOfWeek: DayOfWeek, minTimestamp: Long = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000): Flow<List<AlarmHistoryEntity>>

    @Query("SELECT * FROM alarm_history ORDER BY triggeredAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<AlarmHistoryEntity>>

    @Insert
    suspend fun insertHistory(history: AlarmHistoryEntity)

    @Query("SELECT COUNT(*) FROM alarm_history WHERE alarmId = :alarmId")
    suspend fun getAlarmTriggerCount(alarmId: Int): Int

    @Query("""
        SELECT AVG(
            CASE 
                WHEN userAction = 'DISMISSED' THEN (actionTime - triggeredAt) 
                ELSE 0 
            END
        ) 
        FROM alarm_history 
        WHERE alarmId = :alarmId AND userAction = 'DISMISSED'
    """)
    suspend fun getAverageTimeToDisable(alarmId: Int): Long?

    @Query("DELETE FROM alarm_history WHERE triggeredAt < :timestamp")
    suspend fun deleteHistoryOlderThan(timestamp: Long)

    @Query("SELECT * FROM alarm_history WHERE triggeredAt BETWEEN :startTime AND :endTime")
    suspend fun getHistoryBetween(startTime: Long, endTime: Long): List<AlarmHistoryEntity>
} 