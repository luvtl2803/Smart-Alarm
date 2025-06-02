package com.anhq.smartalarm.core.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.anhq.smartalarm.features.setting.data.SettingDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import javax.inject.Inject

/**
 * BroadcastReceiver to handle snooze functionality for alarms.
 */
@AndroidEntryPoint
class SnoozeReceiver : BroadcastReceiver() {
    @Inject
    lateinit var settingDataStore: SettingDataStore

    companion object {
        private const val TAG = "SnoozeReceiver"
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", -1)
        if (alarmId == -1) return

        // Get snooze settings from SettingDataStore
        val settings = runBlocking {
            settingDataStore.settingsFlow.first()
        }
        
        val snoozeDuration = settings.snoozeDurationMinutes
        val maxSnoozeCount = settings.maxSnoozeCount

        // Get current snooze count
        val currentSnoozeCount = intent.getIntExtra("snooze_count", 0)
        if (currentSnoozeCount >= maxSnoozeCount) {
            Log.d(TAG, "Maximum snooze count reached for alarm $alarmId")
            return
        }

        // Schedule next alarm
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, snoozeDuration)
        }

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("snooze_count", currentSnoozeCount + 1)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                    pendingIntent
                )
            }
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                pendingIntent
            )
        }

        // Cancel current notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(alarmId)

        Log.d(
            TAG,
            "Alarm snoozed for $snoozeDuration minutes. Snooze count: ${currentSnoozeCount + 1}/$maxSnoozeCount"
        )
    }
}
