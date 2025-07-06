package com.anhq.smartalarm.core.data.repository

import android.util.Log
import com.anhq.smartalarm.core.database.dao.AlarmHistoryDao
import com.anhq.smartalarm.core.database.dao.AlarmSuggestionDao
import com.anhq.smartalarm.core.database.model.AlarmSuggestionEntity
import com.anhq.smartalarm.core.model.DayOfWeek
import com.anhq.smartalarm.core.utils.AlarmSuggestionAnalyzer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AlarmSuggestionRepository @Inject constructor(
    private val alarmSuggestionDao: AlarmSuggestionDao,
    private val alarmHistoryDao: AlarmHistoryDao,
    private val suggestionAnalyzer: AlarmSuggestionAnalyzer
) {
    companion object {
        private const val TAG = "AlarmSuggestionRepo"
    }

    fun getSuggestionsForDay(dayOfWeek: DayOfWeek): Flow<List<AlarmSuggestionEntity>> =
        alarmSuggestionDao.getSuggestionsForDay(dayOfWeek)

    suspend fun updateSuggestions(dayOfWeek: DayOfWeek) {
        Log.d(TAG, "Updating suggestions for $dayOfWeek")

        val history = alarmHistoryDao.getAlarmHistoryByDay(dayOfWeek).first()
        Log.d(TAG, "Found ${history.size} history entries for $dayOfWeek")
        if (history.isEmpty()) return

        val newSuggestions = suggestionAnalyzer.analyzePatterns(history, dayOfWeek)
        Log.d(TAG, "Generated ${newSuggestions.size} suggestions for $dayOfWeek")

        newSuggestions.forEach { suggestion ->
            val existing = alarmSuggestionDao.findExistingSuggestion(
                suggestion.hour,
                suggestion.minute,
                suggestion.dayOfWeek
            )

            if (existing != null) {
                Log.d(TAG, "Updating existing suggestion: $suggestion")
                alarmSuggestionDao.updateSuggestion(
                    suggestion.copy(
                        id = existing.id,
                        suggestedCount = existing.suggestedCount,
                        acceptedCount = existing.acceptedCount
                    )
                )
            } else {
                Log.d(TAG, "Inserting new suggestion: $suggestion")
                alarmSuggestionDao.insertSuggestion(suggestion)
            }
        }

        cleanupSuggestions()
    }

    suspend fun markSuggestionUsed(suggestion: AlarmSuggestionEntity, wasAccepted: Boolean) {
        val updated = suggestion.copy(
            suggestedCount = suggestion.suggestedCount + 1,
            acceptedCount = if (wasAccepted) suggestion.acceptedCount + 1 else suggestion.acceptedCount,
            lastUpdated = System.currentTimeMillis()
        )
        alarmSuggestionDao.updateSuggestion(updated)
    }

    private suspend fun cleanupSuggestions() {
        alarmSuggestionDao.deleteLowConfidenceSuggestions(0.3f)

        val oldTimestamp = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        alarmSuggestionDao.deleteOldSuggestions(oldTimestamp)
    }
} 