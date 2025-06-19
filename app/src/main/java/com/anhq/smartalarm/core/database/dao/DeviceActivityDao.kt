package com.anhq.smartalarm.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.anhq.smartalarm.core.database.model.DeviceActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceActivityDao {
    @Insert
    suspend fun insertActivity(activity: DeviceActivityEntity)

    @Query("SELECT * FROM device_activity WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getActivityBetween(startTime: Long, endTime: Long): Flow<List<DeviceActivityEntity>>

    @Query("DELETE FROM device_activity WHERE timestamp < :timestamp")
    suspend fun deleteActivityOlderThan(timestamp: Long)

    @Query("DELETE FROM device_activity WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun deleteActivityBetween(startTime: Long, endTime: Long)

    @Query("DELETE FROM device_activity")
    suspend fun clearAll()
} 