package com.anhq.smartalarm.core.utils

import android.util.Log
import com.anhq.smartalarm.core.database.model.AlarmHistoryEntity
import com.anhq.smartalarm.core.database.model.AlarmSuggestionEntity
import com.anhq.smartalarm.core.model.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.max
import kotlin.math.min

class AlarmSuggestionAnalyzer {
    companion object {
        private const val TAG = "AlarmSuggestionAnalyzer"
        private const val MIN_HISTORY_COUNT = 3
        private const val RECENCY_WEIGHT = 0.6f
        private const val FREQUENCY_WEIGHT = 0.4f
        private const val TIME_WINDOW_MINUTES = 30
        private const val RESPONSE_TIME_WEIGHT = 0.3f
        private const val MAX_ACCEPTABLE_RESPONSE_TIME = 5 * 60 * 1000L
    }

    data class TimePattern(
        val hour: Int,
        val minute: Int,
        val frequency: Int,
        val averageResponseTime: Long,
        val recentCount: Int
    )

    fun analyzePatterns(
        history: List<AlarmHistoryEntity>,
        dayOfWeek: DayOfWeek
    ): List<AlarmSuggestionEntity> {
        Log.d(TAG, "Analyzing patterns for $dayOfWeek with ${history.size} history entries")
        
        if (history.size < MIN_HISTORY_COUNT) {
            Log.d(TAG, "Not enough history entries (minimum $MIN_HISTORY_COUNT required)")
            return emptyList()
        }

        val historyByDay = history.filter { it.dayOfWeek == dayOfWeek }
        Log.d(TAG, "Found ${historyByDay.size} entries for $dayOfWeek")
        
        if (historyByDay.isEmpty()) {
            Log.d(TAG, "No history entries for $dayOfWeek")
            return emptyList()
        }

        val timePatterns = findTimePatterns(historyByDay)
        Log.d(TAG, "Found ${timePatterns.size} time patterns")

        val suggestions = timePatterns.map { pattern ->
            val confidence = calculateConfidence(pattern, historyByDay.size)
            Log.d(TAG, "Pattern ${pattern.hour}:${pattern.minute} has confidence $confidence")
            
            AlarmSuggestionEntity(
                hour = pattern.hour,
                minute = pattern.minute,
                dayOfWeek = dayOfWeek,
                confidence = confidence,
                lastUpdated = System.currentTimeMillis(),
                suggestedCount = 0,
                acceptedCount = 0
            )
        }.filter { it.confidence >= 0.3f }

        Log.d(TAG, "Generated ${suggestions.size} suggestions with confidence >= 0.3")
        return suggestions
    }

    private fun findTimePatterns(history: List<AlarmHistoryEntity>): List<TimePattern> {
        val timeGroups = mutableMapOf<Pair<Int, Int>, MutableList<AlarmHistoryEntity>>()

        history.forEach { entry ->
            val instant = Instant.ofEpochMilli(entry.triggeredAt)
            val localTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            val entryMinutes = localTime.hour * 60 + localTime.minute
            
            Log.d(TAG, "Processing entry at ${localTime.hour}:${localTime.minute}")

            val matchingGroup = timeGroups.keys.firstOrNull { (hour, minute) ->
                val groupMinutes = hour * 60 + minute
                val timeDiff = Math.abs(groupMinutes - entryMinutes)
                Log.d(TAG, "Checking group $hour:$minute, diff=$timeDiff minutes")
                timeDiff <= TIME_WINDOW_MINUTES
            }

            if (matchingGroup != null) {
                timeGroups[matchingGroup]?.add(entry)
                Log.d(TAG, "Added entry to existing group ${matchingGroup.first}:${matchingGroup.second}")
            } else {
                timeGroups[Pair(localTime.hour, localTime.minute)] = mutableListOf(entry)
                Log.d(TAG, "Created new group for ${localTime.hour}:${localTime.minute}")
            }
        }

        Log.d(TAG, "Found ${timeGroups.size} time groups")

        return timeGroups.map { (_, entries) ->

            val avgMinutes = entries.map { entry ->
                val instant = Instant.ofEpochMilli(entry.triggeredAt)
                val localTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                localTime.hour * 60 + localTime.minute
            }.average()

            val avgHour = (avgMinutes / 60).toInt()
            val avgMinute = (avgMinutes % 60).toInt()
            
            Log.d(TAG, "Group average time: $avgHour:$avgMinute from ${entries.size} entries")

            val recentThreshold = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            val recentEntries = entries.count { it.triggeredAt >= recentThreshold }

            val dismissedEntries = entries.filter { it.userAction == "DISMISSED" }
            val avgResponseTime = if (dismissedEntries.isNotEmpty()) {
                dismissedEntries.map { it.actionTime - it.triggeredAt }.average().toLong()
            } else {
                MAX_ACCEPTABLE_RESPONSE_TIME
            }
            
            TimePattern(
                hour = avgHour,
                minute = avgMinute,
                frequency = entries.size,
                averageResponseTime = avgResponseTime,
                recentCount = recentEntries
            ).also {
                Log.d(TAG, "Created pattern: ${it.hour}:${it.minute}, frequency=${it.frequency}, recentCount=${it.recentCount}")
            }
        }.sortedByDescending { it.frequency }
    }

    private fun calculateConfidence(pattern: TimePattern, totalHistorySize: Int): Float {

        val frequencyScore = min(1f, pattern.frequency.toFloat() / totalHistorySize)

        val recencyScore = min(1f, pattern.recentCount.toFloat() / max(1, pattern.frequency))

        val responseTimeScore = if (pattern.averageResponseTime <= MAX_ACCEPTABLE_RESPONSE_TIME) {
            1f - (pattern.averageResponseTime.toFloat() / MAX_ACCEPTABLE_RESPONSE_TIME)
        } else {
            0f
        }

        val confidence = (FREQUENCY_WEIGHT * frequencyScore + 
                RECENCY_WEIGHT * recencyScore +
                RESPONSE_TIME_WEIGHT * responseTimeScore) / 
                (FREQUENCY_WEIGHT + RECENCY_WEIGHT + RESPONSE_TIME_WEIGHT)

        Log.d(TAG, """
            Confidence calculation for ${pattern.hour}:${pattern.minute}:
            - Frequency score: $frequencyScore
            - Recency score: $recencyScore
            - Response time score: $responseTimeScore
            - Final confidence: $confidence
        """.trimIndent())

        return confidence
    }
} 