package com.anhq.smartalarm.core.data.repository

import android.util.Log
import com.anhq.smartalarm.core.database.dao.AlarmHistoryDao
import com.anhq.smartalarm.core.database.dao.AlarmSuggestionDao
import com.anhq.smartalarm.core.database.model.AlarmSuggestionEntity
import com.anhq.smartalarm.core.model.DayOfWeek
import com.anhq.smartalarm.core.utils.AlarmSuggestionAnalyzer
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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

    fun getHighConfidenceSuggestions(minConfidence: Float = 0.7f): Flow<List<AlarmSuggestionEntity>> =
        alarmSuggestionDao.getHighConfidenceSuggestions(minConfidence)

    suspend fun updateSuggestions(dayOfWeek: DayOfWeek) {
        Log.d(TAG, "Updating suggestions for $dayOfWeek")
        
        // Get all history for analysis using first() to collect the Flow
        val history = alarmHistoryDao.getAlarmHistoryByDay(dayOfWeek).first()
        Log.d(TAG, "Found ${history.size} history entries for $dayOfWeek")
        if (history.isEmpty()) return

        // Generate new suggestions
        val newSuggestions = suggestionAnalyzer.analyzePatterns(history, dayOfWeek)
        Log.d(TAG, "Generated ${newSuggestions.size} suggestions for $dayOfWeek")

        // Update or insert each suggestion
        newSuggestions.forEach { suggestion ->
            val existing = alarmSuggestionDao.findExistingSuggestion(
                suggestion.hour,
                suggestion.minute,
                suggestion.dayOfWeek
            )

            if (existing != null) {
                // Update existing suggestion
                Log.d(TAG, "Updating existing suggestion: $suggestion")
                alarmSuggestionDao.updateSuggestion(
                    suggestion.copy(
                        id = existing.id,
                        suggestedCount = existing.suggestedCount,
                        acceptedCount = existing.acceptedCount
                    )
                )
            } else {
                // Insert new suggestion
                Log.d(TAG, "Inserting new suggestion: $suggestion")
                alarmSuggestionDao.insertSuggestion(suggestion)
            }
        }

        // Cleanup old/low confidence suggestions
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
        // Remove suggestions with low confidence
        alarmSuggestionDao.deleteLowConfidenceSuggestions(0.3f)

        // Remove suggestions not updated in last 30 days
        val oldTimestamp = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        alarmSuggestionDao.deleteOldSuggestions(oldTimestamp)
    }
} 