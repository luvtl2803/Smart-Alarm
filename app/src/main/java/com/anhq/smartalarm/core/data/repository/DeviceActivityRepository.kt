package com.anhq.smartalarm.core.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.anhq.smartalarm.core.database.dao.DeviceActivityDao
import com.anhq.smartalarm.core.database.model.DeviceActivityEntity
import com.anhq.smartalarm.core.sharereference.PreferenceHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceActivityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceActivityDao: DeviceActivityDao,
    private val preferenceHelper: PreferenceHelper
) {
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    companion object {
        private const val TAG = "DeviceActivityRepo"
        private const val FLEXIBLE_TIME_MINUTES = 120
    }

    suspend fun syncDeviceActivity(startTime: Long, endTime: Long) {
        try {
            Log.d(TAG, "Starting sync from " + formatTime(startTime) + " to " + formatTime(endTime))
            deviceActivityDao.deleteActivityBetween(startTime, endTime)

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            ).filter { it.totalTimeInForeground > 0 }
                .sortedBy { it.lastTimeUsed }

            Log.d(TAG, "Found ${usageStats.size} total usage stats records")

            val sleepHour = preferenceHelper.sleepTimeHour
            val sleepMinute = preferenceHelper.sleepTimeMinute
            val sleepTimeInMinutes = sleepHour * 60 + sleepMinute
            val flexibleStart = sleepTimeInMinutes - FLEXIBLE_TIME_MINUTES
            val flexibleEnd = sleepTimeInMinutes + FLEXIBLE_TIME_MINUTES

            val usageByDay = usageStats.groupBy {
                val localDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(it.lastTimeUsed),
                    ZoneId.systemDefault()
                ).toLocalDate()
                localDate
            }

            for ((_, stats) in usageByDay) {
                val lastUsage = stats.lastOrNull { stat ->
                    val localTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(stat.lastTimeUsed),
                        ZoneId.systemDefault()
                    )
                    val minutes = localTime.hour * 60 + localTime.minute
                    minutes in flexibleStart..flexibleEnd
                }
                if (lastUsage != null) {
                    val localTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(lastUsage.lastTimeUsed),
                        ZoneId.systemDefault()
                    )
                    deviceActivityDao.insertActivity(
                        DeviceActivityEntity(
                            timestamp = lastUsage.lastTimeUsed,
                            isActive = false,
                            dayOfWeek = localTime.dayOfWeek.value
                        )
                    )
                    Log.d(TAG, "✨ Recording sleep start at ${formatTime(lastUsage.lastTimeUsed)}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error syncing device activity", e)
        }
    }

    private fun formatTime(timestamp: Long): String {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        ).format(dateFormatter)
    }
}
