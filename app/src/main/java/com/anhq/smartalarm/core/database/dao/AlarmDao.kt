package com.anhq.smartalarm.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anhq.smartalarm.core.database.model.AlarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarmEntity: AlarmEntity) : Long
    @Query("UPDATE alarm_entity SET isActive = :isActive WHERE id = :id")
    suspend fun updateAlarmStatus(id: Int, isActive: Boolean)
    @Update
    suspend fun updateAlarm(alarmEntity: AlarmEntity)
    @Query("SELECT * FROM alarm_entity WHERE id = :alarmId")
    fun getAlarmById(alarmId: Int): Flow<AlarmEntity>
    @Query("SELECT * FROM alarm_entity")
    fun getAlarms(): Flow<List<AlarmEntity>>
}