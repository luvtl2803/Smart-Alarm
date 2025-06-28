package com.anhq.smartalarm.core.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.anhq.smartalarm.R
import com.anhq.smartalarm.core.data.repository.AlarmRepository
import com.anhq.smartalarm.core.model.DayOfWeek
import com.anhq.smartalarm.core.utils.AlarmReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class RestoreAlarmsService : Service() {
    @Inject
    lateinit var alarmRepository: AlarmRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "RestoreAlarmsService"
        private const val CHANNEL_ID = "restore_alarms_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Delay moving to foreground to avoid ForegroundServiceStartNotAllowedException
        handler.postDelayed({
            try {
                startForeground(NOTIFICATION_ID, createNotification())
            } catch (e: Exception) {
                Log.e(TAG, "Error starting foreground", e)
            }
        }, 1000)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        restoreAlarms()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val name = "Khôi phục báo thức"
        val descriptionText = "Đang khôi phục lại các báo thức"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Khôi phục báo thức")
        .setContentText("Đang khôi phục lại các báo thức của bạn")
        .setSmallIcon(R.drawable.ic_alarm)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    private fun restoreAlarms() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        scope.launch {
            try {
                val alarms = alarmRepository.getAllAlarms().first()
                Log.d(TAG, "Found ${alarms.size} alarms to restore")

                alarms.filter { it.isActive }.forEach { alarm ->
                    val alarmIntent =
                        Intent(this@RestoreAlarmsService, AlarmReceiver::class.java).apply {
                            putExtra("alarm_id", alarm.id)
                            putExtra("game_type", alarm.gameType.ordinal)
                        }

                    if (alarm.selectedDays.isEmpty()) {
                        // Single alarm
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, alarm.hour)
                            set(Calendar.MINUTE, alarm.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        if (calendar.timeInMillis < System.currentTimeMillis()) {
                            calendar.add(Calendar.DAY_OF_MONTH, 1)
                        }

                        val pendingIntent = PendingIntent.getBroadcast(
                            this@RestoreAlarmsService,
                            alarm.id,
                            alarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        alarmManager.setAlarmClock(
                            AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                            pendingIntent
                        )
                        Log.d(
                            TAG,
                            "Restored single alarm: ${alarm.id} for ${alarm.hour}:${alarm.minute}"
                        )
                    } else {
                        // Repeating alarms
                        alarm.selectedDays.forEach { dayOfWeek ->
                            val calendar = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, alarm.hour)
                                set(Calendar.MINUTE, alarm.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            val targetDayOfWeek = when (dayOfWeek) {
                                DayOfWeek.SUN -> Calendar.SUNDAY
                                DayOfWeek.MON -> Calendar.MONDAY
                                DayOfWeek.TUE -> Calendar.TUESDAY
                                DayOfWeek.WED -> Calendar.WEDNESDAY
                                DayOfWeek.THU -> Calendar.THURSDAY
                                DayOfWeek.FRI -> Calendar.FRIDAY
                                DayOfWeek.SAT -> Calendar.SATURDAY
                            }

                            val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
                            var daysUntilNext = (targetDayOfWeek - currentDay + 7) % 7

                            if (daysUntilNext == 0 && calendar.timeInMillis <= System.currentTimeMillis()) {
                                daysUntilNext = 7
                            }

                            calendar.add(Calendar.DAY_OF_MONTH, daysUntilNext)

                            val uniqueId = alarm.id * 10 + dayOfWeek.ordinal
                            val pendingIntent = PendingIntent.getBroadcast(
                                this@RestoreAlarmsService,
                                uniqueId,
                                alarmIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            alarmManager.setAlarmClock(
                                AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                                pendingIntent
                            )
                            Log.d(
                                TAG,
                                "Restored repeating alarm: ${alarm.id} for ${dayOfWeek.name} at ${alarm.hour}:${alarm.minute}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring alarms", e)
            } finally {
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        job.cancel()
    }
} 