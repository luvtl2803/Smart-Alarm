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
import com.anhq.smartalarm.core.data.repository.AlarmHistoryRepository
import com.anhq.smartalarm.core.data.repository.AlarmRepository
import com.anhq.smartalarm.core.data.repository.AlarmSuggestionRepository
import com.anhq.smartalarm.core.model.DayOfWeek
import com.anhq.smartalarm.core.sharereference.PreferenceHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import javax.inject.Inject

/**
 * BroadcastReceiver to handle snooze functionality for alarms.
 */
@AndroidEntryPoint
class SnoozeReceiver : BroadcastReceiver() {
    @Inject
    lateinit var preferenceHelper: PreferenceHelper
    
    @Inject
    lateinit var alarmHistoryRepository: AlarmHistoryRepository

    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var alarmSuggestionRepository: AlarmSuggestionRepository

    companion object {
        private const val TAG = "SnoozeReceiver"
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", -1)
        val triggeredAt = intent.getLongExtra("triggered_at", System.currentTimeMillis())
        
        if (alarmId == -1) return

        AlarmReceiver.stopAlarm()

        val settings = runBlocking {
            preferenceHelper.settingsFlow.first()
        }
        
        val snoozeDuration = settings.snoozeDurationMinutes
        val maxSnoozeCount = settings.maxSnoozeCount

        val currentSnoozeCount = intent.getIntExtra("snooze_count", 0)
        if (currentSnoozeCount >= maxSnoozeCount) {
            Log.d(TAG, "Maximum snooze count reached for alarm $alarmId")
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(alarmId)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            alarmHistoryRepository.recordAlarmHistory(
                alarmId = alarmId,
                userAction = "SNOOZED",
                triggeredAt = triggeredAt
            )

            val alarm = alarmRepository.getAlarmById(alarmId).first()
            alarm?.let {
                if (it.selectedDays.isNotEmpty()) {
                    it.selectedDays.forEach { day ->
                        alarmSuggestionRepository.updateSuggestions(day)
                    }
                } else {
                    val calendar = Calendar.getInstance()
                    val calendarDay = calendar.get(Calendar.DAY_OF_WEEK)
                    val dayOfWeek = when (calendarDay) {
                        Calendar.MONDAY -> DayOfWeek.MON
                        Calendar.TUESDAY -> DayOfWeek.TUE
                        Calendar.WEDNESDAY -> DayOfWeek.WED
                        Calendar.THURSDAY -> DayOfWeek.THU
                        Calendar.FRIDAY -> DayOfWeek.FRI
                        Calendar.SATURDAY -> DayOfWeek.SAT
                        Calendar.SUNDAY -> DayOfWeek.SUN
                        else -> DayOfWeek.MON
                    }
                    alarmSuggestionRepository.updateSuggestions(dayOfWeek)
                }
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, snoozeDuration)
        }

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("snooze_count", currentSnoozeCount + 1)
            putExtra("is_repeating", intent.getBooleanExtra("is_repeating", false))
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

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(alarmId)

        Log.d(
            TAG,
            "Alarm snoozed for $snoozeDuration minutes. Snooze count: ${currentSnoozeCount + 1}/$maxSnoozeCount"
        )
    }
}
