package com.anhq.smartalarm.core.data.repository

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import com.anhq.smartalarm.core.database.dao.AlarmHistoryDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDateTime
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmHistoryDao: AlarmHistoryDao,
    private val deviceActivityRepository: DeviceActivityRepository
) {

    companion object {
        private const val TAG = "SleepRepository"
    }

    fun hasPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            ) == AppOpsManager.MODE_ALLOWED
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            ) == AppOpsManager.MODE_ALLOWED
        }
        Log.d(TAG, "Has permission: $hasPermission")
        return hasPermission
    }

    fun getPermissionIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    fun getEnhancedSleepData(days: Int = 7): Flow<List<EnhancedSleepData>> = flow {
        try {
            if (!hasPermission()) {
                Log.d(TAG, "No permission, returning empty list")
                emit(emptyList())
                return@flow
            }

            // Sync device activity data
            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, -days)
            val startTime = calendar.timeInMillis

            Log.d(
                TAG,
                "Syncing device activity from ${Instant.ofEpochMilli(startTime)} to ${
                    Instant.ofEpochMilli(endTime)
                }"
            )
            deviceActivityRepository.syncDeviceActivity(startTime, endTime)

            // Combine sleep data with alarm history
            deviceActivityRepository.getActivityBetween(startTime, endTime).collect { sleepData ->
                Log.d(TAG, "Got ${sleepData.size} sleep periods")
                val enhancedData = sleepData.map { sleep ->
                    val endTimestamp = sleep.endTime.toEpochMilli()

                    // Tìm báo thức trong khoảng thời gian từ lúc thức dậy trở về trước 30 phút
                    val alarmStartTime = endTimestamp - (30 * 60 * 1000) // 30 phút trước khi thức
                    val alarmEndTime = endTimestamp + (5 * 60 * 1000) // 5 phút sau khi thức

                    val dayAlarms = alarmHistoryDao.getHistoryBetween(
                        alarmStartTime,
                        alarmEndTime
                    )
                    Log.d(TAG, "Searching alarms for period ${sleep.date}:")
                    Log.d(TAG, "End time: $endTimestamp")
                    Log.d(TAG, "Search range: $alarmStartTime to $alarmEndTime")
                    Log.d(TAG, "Found ${dayAlarms.size} alarms")

                    EnhancedSleepData(
                        date = sleep.date,
                        durationMinutes = sleep.durationMinutes,
                        startTime = sleep.startTime,
                        endTime = sleep.endTime,
                        alarmTriggerTime = dayAlarms.firstOrNull()?.let {
                            Instant.ofEpochMilli(it.triggeredAt)
                        },
                        userAction = dayAlarms.lastOrNull()?.userAction,
                        timeToAction = dayAlarms.lastOrNull()?.let {
                            it.actionTime - it.triggeredAt
                        },
                        snoozeCount = dayAlarms.count { it.userAction == "SNOOZED" }
                    )
                }
                Log.d(TAG, "Emitting ${enhancedData.size} enhanced sleep periods")
                emit(enhancedData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting enhanced sleep data", e)
            emit(emptyList())
        }
    }
}

data class SleepData(
    val date: LocalDateTime,
    val durationMinutes: Long,
    val startTime: Instant,
    val endTime: Instant
)
