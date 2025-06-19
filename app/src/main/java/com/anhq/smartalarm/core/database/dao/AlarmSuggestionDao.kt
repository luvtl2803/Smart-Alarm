package com.anhq.smartalarm.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.anhq.smartalarm.core.database.model.AlarmSuggestionEntity
import com.anhq.smartalarm.core.model.DayOfWeek
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmSuggestionDao {
    @Query("SELECT * FROM alarm_suggestions WHERE dayOfWeek = :dayOfWeek ORDER BY confidence DESC")
    fun getSuggestionsForDay(dayOfWeek: DayOfWeek): Flow<List<AlarmSuggestionEntity>>

    @Query("SELECT * FROM alarm_suggestions WHERE confidence >= :minConfidence ORDER BY confidence DESC")
    fun getHighConfidenceSuggestions(minConfidence: Float = 0.7f): Flow<List<AlarmSuggestionEntity>>

    @Insert
    suspend fun insertSuggestion(suggestion: AlarmSuggestionEntity)

    @Update
    suspend fun updateSuggestion(suggestion: AlarmSuggestionEntity)

    @Query("""
        SELECT * FROM alarm_suggestions 
        WHERE hour = :hour AND minute = :minute AND dayOfWeek = :dayOfWeek 
        LIMIT 1
    """)
    suspend fun findExistingSuggestion(hour: Int, minute: Int, dayOfWeek: DayOfWeek): AlarmSuggestionEntity?

    @Query("DELETE FROM alarm_suggestions WHERE confidence < :minConfidence")
    suspend fun deleteLowConfidenceSuggestions(minConfidence: Float = 0.3f)

    @Query("DELETE FROM alarm_suggestions WHERE lastUpdated < :timestamp")
    suspend fun deleteOldSuggestions(timestamp: Long)
} 