package com.anhq.smartalarm.core.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.anhq.smartalarm.core.database.dao.DeviceActivityDao
import com.anhq.smartalarm.core.database.model.DeviceActivityEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceActivityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceActivityDao: DeviceActivityDao
) {
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    companion object {
        private const val TAG = "DeviceActivityRepo"
    }

    suspend fun syncDeviceActivity(startTime: Long, endTime: Long) {
        try {
            deviceActivityDao.deleteActivityBetween(startTime, endTime)

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                startTime,
                endTime
            ).sortedBy { it.lastTimeUsed }

            Log.d(TAG, "Found ${usageStats.size} usage stats records")

            val activeStats = usageStats.filter { it.totalTimeInForeground > 0 }
            var isInDeepSleep = false

            for (i in 0 until activeStats.size - 1) {
                val currentStat = activeStats[i]
                val nextStat = activeStats[i + 1]
                val currentTime = currentStat.lastTimeUsed
                val nextTime = nextStat.lastTimeUsed
                val inactiveDuration = nextTime - currentTime
                val currentLocalTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(currentTime),
                    ZoneId.systemDefault()
                )

//                Log.d(
//                    TAG, "Checking gap: from=$currentLocalTime, " +
//                            "package=${currentStat.packageName}, " +
//                            "inactiveDuration=${inactiveDuration / 1000 / 60}m"
//                )

                if (inactiveDuration >= 30 * 60 * 1000) {
                    val hour = currentLocalTime.hour
                    if ((hour in 21..23 || hour in 0..4) && !isInDeepSleep) {
                        deviceActivityDao.insertActivity(
                            DeviceActivityEntity(
                                timestamp = currentTime,
                                isActive = false,
                                dayOfWeek = currentLocalTime.dayOfWeek.value
                            )
                        )
                        Log.d(TAG, "Recording sleep start at $currentLocalTime")
                        isInDeepSleep = true
                    }
                }

                if (isInDeepSleep && nextStat.totalTimeInForeground > 0) {
                    val wakeTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(nextTime),
                        ZoneId.systemDefault()
                    )
                    deviceActivityDao.insertActivity(
                        DeviceActivityEntity(
                            timestamp = nextTime,
                            isActive = true,
                            dayOfWeek = wakeTime.dayOfWeek.value
                        )
                    )
                    Log.d(TAG, "Recording wake up at $wakeTime")
                    isInDeepSleep = false
                }
            }

            if (isInDeepSleep) {
                val endLocalTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(endTime),
                    ZoneId.systemDefault()
                )
                deviceActivityDao.insertActivity(
                    DeviceActivityEntity(
                        timestamp = endTime,
                        isActive = true,
                        dayOfWeek = endLocalTime.dayOfWeek.value
                    )
                )
                Log.d(TAG, "Recording final wake up at $endLocalTime")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing device activity", e)
        }
    }

    fun getActivityBetween(startTime: Long, endTime: Long): Flow<List<SleepData>> {
        Log.d(TAG, "Getting activity between $startTime and $endTime")
        return deviceActivityDao.getActivityBetween(startTime, endTime)
            .map { activities ->
                val sleepPeriods = mutableListOf<SleepData>()
                var sleepStart: Long? = null

                activities.forEach { activity ->
                    if (!activity.isActive && sleepStart == null) {
                        sleepStart = activity.timestamp
                        Log.d(TAG, "Found sleep start at ${Instant.ofEpochMilli(activity.timestamp)}")
                    } else if (activity.isActive && sleepStart != null) {
                        val startDateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(sleepStart!!),
                            ZoneId.systemDefault()
                        )
                        val duration = activity.timestamp - sleepStart!!

                        if (isValidSleepPeriod(startDateTime, duration)) {
                            sleepPeriods.add(
                                SleepData(
                                    date = startDateTime,
                                    durationMinutes = duration / (60 * 1000),
                                    startTime = Instant.ofEpochMilli(sleepStart!!),
                                    endTime = Instant.ofEpochMilli(activity.timestamp)
                                )
                            )
                            Log.d(TAG, "Added valid sleep period: ${sleepPeriods.last()}")
                        } else {
                            Log.d(TAG, "Invalid sleep period: start=$startDateTime, duration=${duration}ms")
                        }
                        sleepStart = null
                    }
                }

                Log.d(TAG, "Returning ${sleepPeriods.size} sleep periods")
                sleepPeriods
            }
    }

    private fun isValidSleepPeriod(startDateTime: LocalDateTime, duration: Long): Boolean {
        val startHour = startDateTime.hour
        val isValidStart = startHour in 21..23 || startHour in 0..4
        val durationHours = duration / (60 * 60 * 1000)
        val isValidDuration = durationHours in 2..14

        Log.d(TAG, "Validating sleep period: localTime=$startDateTime, durationHours=$durationHours")
        return isValidStart && isValidDuration
    }
}
