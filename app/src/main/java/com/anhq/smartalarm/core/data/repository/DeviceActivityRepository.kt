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
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    companion object {
        private const val DEEP_SLEEP_THRESHOLD = 15L * 60 * 1000 // 15 minutes without activity
        private const val TAG = "DeviceActivityRepo"
    }

    suspend fun syncDeviceActivity(startTime: Long, endTime: Long) {
        try {
            // Xóa dữ liệu cũ trong khoảng thời gian này
            deviceActivityDao.deleteActivityBetween(startTime, endTime)

            // Lấy usage stats từ hệ thống
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                startTime,
                endTime
            ).sortedBy { it.lastTimeUsed }

            Log.d(TAG, "Found ${usageStats.size} usage stats records")

            var lastActiveTime = startTime
            var isInDeepSleep = false

            for (stat in usageStats) {
                val currentTime = stat.lastTimeUsed
                val activeTime = stat.totalTimeInForeground

                if (activeTime > 0) {
                    // Device was active
                    if (isInDeepSleep) {
                        // Kết thúc giấc ngủ
                        val activity = DeviceActivityEntity(
                            timestamp = currentTime,
                            isActive = true,
                            dayOfWeek = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(currentTime),
                                ZoneId.systemDefault()
                            ).dayOfWeek.value
                        )
                        deviceActivityDao.insertActivity(activity)
                        Log.d(TAG, "Inserted wake activity: $activity")
                    }
                    isInDeepSleep = false
                    lastActiveTime = currentTime
                } else {
                    // Check for deep sleep entry
                    val inactiveDuration = currentTime - lastActiveTime
                    if (!isInDeepSleep && inactiveDuration >= DEEP_SLEEP_THRESHOLD) {
                        // Bắt đầu giấc ngủ
                        val activity = DeviceActivityEntity(
                            timestamp = lastActiveTime,
                            isActive = false,
                            dayOfWeek = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(lastActiveTime),
                                ZoneId.systemDefault()
                            ).dayOfWeek.value
                        )
                        deviceActivityDao.insertActivity(activity)
                        Log.d(TAG, "Inserted sleep activity: $activity")
                        isInDeepSleep = true
                    }
                }
            }

            // Nếu đang trong trạng thái ngủ khi kết thúc, thêm một bản ghi kết thúc
            if (isInDeepSleep) {
                val activity = DeviceActivityEntity(
                    timestamp = endTime,
                    isActive = true,
                    dayOfWeek = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(endTime),
                        ZoneId.systemDefault()
                    ).dayOfWeek.value
                )
                deviceActivityDao.insertActivity(activity)
                Log.d(TAG, "Inserted final wake activity: $activity")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing device activity", e)
            e.printStackTrace()
        }
    }

    fun getActivityBetween(startTime: Long, endTime: Long): Flow<List<SleepData>> {
        Log.d(TAG, "Getting activity between $startTime and $endTime")
        return deviceActivityDao.getActivityBetween(startTime, endTime)
            .map { activities ->
                Log.d(TAG, "Found ${activities.size} activity records")
                val sleepPeriods = mutableListOf<SleepData>()
                var sleepStart: Long? = null

                activities.forEach { activity ->
                    if (!activity.isActive && sleepStart == null) {
                        // Bắt đầu giấc ngủ
                        sleepStart = activity.timestamp
                        Log.d(TAG, "Found sleep start at ${Instant.ofEpochMilli(activity.timestamp)}")
                    } else if (activity.isActive && sleepStart != null) {
                        // Kết thúc giấc ngủ
                        val startDateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(sleepStart!!),
                            ZoneId.systemDefault()
                        )
                        
                        // Chỉ tính các giấc ngủ hợp lệ (bắt đầu từ tối và kết thúc vào sáng)
                        if (isValidSleepPeriod(startDateTime, activity.timestamp - sleepStart!!)) {
                            val sleepData = SleepData(
                                date = startDateTime,
                                durationMinutes = (activity.timestamp - sleepStart!!) / (60 * 1000),
                                startTime = Instant.ofEpochMilli(sleepStart!!),
                                endTime = Instant.ofEpochMilli(activity.timestamp)
                            )
                            sleepPeriods.add(sleepData)
                            Log.d(TAG, "Added valid sleep period: $sleepData")
                        } else {
                            Log.d(TAG, "Invalid sleep period: start=$startDateTime, duration=${activity.timestamp - sleepStart!!}ms")
                        }
                        sleepStart = null
                    }
                }

                Log.d(TAG, "Returning ${sleepPeriods.size} sleep periods")
                sleepPeriods
            }
    }

    private fun isValidSleepPeriod(startDateTime: LocalDateTime, duration: Long): Boolean {
        // Kiểm tra thời gian bắt đầu (18:00 - 03:00)
        val startHour = startDateTime.hour
        val isValidStart = startHour in 20..23 || startHour in 0..3
        
        // Kiểm tra thời lượng (1-12 giờ)
        val durationHours = duration / (60 * 60 * 1000)
        val isValidDuration = durationHours in 1..12

        Log.d(TAG, "Validating sleep period: startHour=$startHour, durationHours=$durationHours")
        Log.d(TAG, "isValidStart=$isValidStart, isValidDuration=$isValidDuration")

        return !isValidStart && isValidDuration
    }
} 