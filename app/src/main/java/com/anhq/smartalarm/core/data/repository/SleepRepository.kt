package com.anhq.smartalarm.core.data.repository

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    companion object {
        private const val DEEP_SLEEP_THRESHOLD = 15L * 60 * 1000 // 15 minutes without activity
        private const val SLEEP_START_HOUR = 20 // 8 PM
        private const val SLEEP_END_HOUR = 11 // 11 AM
        private const val MIN_SLEEP_DURATION = 3L * 60 * 60 * 1000 // 3 hours minimum sleep
        private const val MAX_SLEEP_DURATION = 12L * 60 * 60 * 1000 // 12 hours maximum sleep
    }

    fun hasPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getPermissionIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    fun getSleepData(days: Int = 7): Flow<List<SleepData>> = flow {
        try {
            if (!hasPermission()) {
                emit(emptyList())
                return@flow
            }

            val sleepData = mutableListOf<SleepData>()
            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis

            // Go back by specified number of days
            calendar.add(Calendar.DAY_OF_YEAR, -days)
            val startTime = calendar.timeInMillis

            // Get usage stats for the time period
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                startTime,
                endTime
            ).sortedBy { it.lastTimeUsed }

            var lastActiveTime = startTime
            var deepSleepStartTime: Long? = null
            var isInDeepSleep = false

            for (stat in usageStats) {
                val currentTime = stat.lastTimeUsed
                val activeTime = stat.totalTimeInForeground

                if (activeTime > 0) {
                    // Device was active
                    if (isInDeepSleep && deepSleepStartTime != null) {
                        val sleepDuration = currentTime - deepSleepStartTime
                        
                        // Convert timestamps to LocalDateTime for better time checking
                        val sleepStartDateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(deepSleepStartTime),
                            ZoneId.systemDefault()
                        )
                        val sleepEndDateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(currentTime),
                            ZoneId.systemDefault()
                        )

                        // Validate sleep period
                        if (isValidSleepPeriod(sleepStartDateTime, sleepEndDateTime, sleepDuration)) {
                            sleepData.add(
                                SleepData(
                                    date = sleepStartDateTime,
                                    durationMinutes = sleepDuration / (60 * 1000),
                                    startTime = Instant.ofEpochMilli(deepSleepStartTime),
                                    endTime = Instant.ofEpochMilli(currentTime)
                                )
                            )
                        }
                    }
                    isInDeepSleep = false
                    deepSleepStartTime = null
                    lastActiveTime = currentTime
                } else {
                    // Check for deep sleep entry
                    val inactiveDuration = currentTime - lastActiveTime
                    if (!isInDeepSleep && inactiveDuration >= DEEP_SLEEP_THRESHOLD) {
                        val potentialSleepStart = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(lastActiveTime),
                            ZoneId.systemDefault()
                        )
                        
                        // Only start tracking sleep if it begins during sleep hours
                        if (potentialSleepStart.hour >= SLEEP_START_HOUR || potentialSleepStart.hour <= 3) {
                            isInDeepSleep = true
                            deepSleepStartTime = lastActiveTime
                        }
                    }
                }
            }

            // Handle case where device is still in deep sleep
            val now = System.currentTimeMillis()
            if (isInDeepSleep && deepSleepStartTime != null) {
                val sleepDuration = now - deepSleepStartTime
                val sleepStartDateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(deepSleepStartTime),
                    ZoneId.systemDefault()
                )
                val sleepEndDateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(now),
                    ZoneId.systemDefault()
                )

                if (isValidSleepPeriod(sleepStartDateTime, sleepEndDateTime, sleepDuration)) {
                    sleepData.add(
                        SleepData(
                            date = sleepStartDateTime,
                            durationMinutes = sleepDuration / (60 * 1000),
                            startTime = Instant.ofEpochMilli(deepSleepStartTime),
                            endTime = Instant.ofEpochMilli(now)
                        )
                    )
                }
            }

            emit(sleepData.sortedBy { it.date })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    private fun isValidSleepPeriod(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        duration: Long
    ): Boolean {
        // Check if duration is within reasonable bounds
        if (duration < MIN_SLEEP_DURATION || duration > MAX_SLEEP_DURATION) {
            return false
        }

        // Sleep must start at night (8 PM - 3 AM)
        val startHour = startDateTime.hour
        if (startHour < SLEEP_START_HOUR && startHour > 3) {
            return false
        }

        // Sleep must end in the morning (5 AM - 11 AM)
        val endHour = endDateTime.hour
        if (endHour < 5 || endHour > SLEEP_END_HOUR) {
            return false
        }

        return true
    }
}

data class SleepData(
    val date: LocalDateTime,
    val durationMinutes: Long,
    val startTime: Instant,
    val endTime: Instant
) 