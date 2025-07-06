package com.anhq.smartalarm.core.data.repository

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import com.anhq.smartalarm.core.database.dao.AlarmHistoryDao
import com.anhq.smartalarm.core.database.dao.DeviceActivityDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmHistoryDao: AlarmHistoryDao,
    private val deviceActivityDao: DeviceActivityDao,
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

            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, -days)
            val startTime = calendar.timeInMillis

            Log.d(
                TAG,
                "Syncing device activity from ${Instant.ofEpochMilli(startTime)} to ${
                    Instant.ofEpochMilli(
                        endTime
                    )
                }"
            )
            deviceActivityRepository.syncDeviceActivity(startTime, endTime)

            val sleepStarts = withContext(Dispatchers.IO) {
                deviceActivityDao.getActivityBetween(startTime, endTime)
                    .first()
                    .filter { !it.isActive }
                    .sortedBy { it.timestamp }
            }

            val result = mutableListOf<EnhancedSleepData>()
            for (sleepStart in sleepStarts) {
                val minEnd = sleepStart.timestamp + 3 * 60 * 60 * 1000L
                val maxEnd = sleepStart.timestamp + 12 * 60 * 60 * 1000L
                val alarms = alarmHistoryDao.getHistoryBetween(minEnd, maxEnd)
                val dismissedAlarm = alarms.firstOrNull { it.userAction == "DISMISSED" }
                if (dismissedAlarm != null) {
                    val durationMinutes =
                        (dismissedAlarm.actionTime - sleepStart.timestamp) / (60 * 1000)
                    if (durationMinutes in (3 * 60)..(12 * 60)) {
                        result.add(
                            EnhancedSleepData(
                                date = LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(sleepStart.timestamp),
                                    java.time.ZoneId.systemDefault()
                                ),
                                durationMinutes = durationMinutes,
                                startTime = Instant.ofEpochMilli(sleepStart.timestamp),
                                endTime = Instant.ofEpochMilli(dismissedAlarm.actionTime),
                                alarmTriggerTime = Instant.ofEpochMilli(dismissedAlarm.triggeredAt),
                                userAction = dismissedAlarm.userAction,
                                timeToAction = dismissedAlarm.actionTime - dismissedAlarm.triggeredAt,
                                snoozeCount = alarms.count { it.userAction == "SNOOZED" }
                            )
                        )
                    }
                }
            }
            emit(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting enhanced sleep data", e)
            emit(emptyList())
        }
    }
}

