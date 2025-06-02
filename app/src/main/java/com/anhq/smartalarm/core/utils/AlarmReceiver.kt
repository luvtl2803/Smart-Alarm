package com.anhq.smartalarm.core.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.anhq.smartalarm.R
import com.anhq.smartalarm.core.data.repository.AlarmRepository
import com.anhq.smartalarm.core.model.Alarm
import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.features.alarm.NoGameAlarmActivity
import com.anhq.smartalarm.features.game.AlarmGameActivity
import com.anhq.smartalarm.features.setting.data.SettingDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * BroadcastReceiver to handle alarm events, including playing an alarm sound and showing notifications.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var alarmRepository: AlarmRepository

    companion object {
        private const val CHANNEL_ID = "alarm_channel"
        private var mediaPlayer: MediaPlayer? = null
        private var vibrator: Vibrator? = null
        private val VIBRATION_PATTERN = longArrayOf(0, 500, 500) // Start, vibrate, sleep pattern
        private const val TAG = "AlarmReceiver"

        /**
         * Stops the currently playing alarm sound and vibration.
         */
        fun stopAlarm() {
            try {
                mediaPlayer?.apply {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                }
                mediaPlayer = null

                vibrator?.cancel()
                vibrator = null

                Log.d(TAG, "Alarm sound and vibration stopped successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping alarm", e)
            }
        }
    }

    @SuppressLint("NotificationPermission", "Wakelock")
    override fun onReceive(context: Context, intent: Intent) {
        // Acquire wake lock to ensure the device stays awake
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "SmartAlarm:AlarmWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L) // 10 minutes max
        }

        try {
            val alarmId = intent.getIntExtra("alarm_id", -1)
            val isRepeating = intent.getBooleanExtra("is_repeating", false)
            val snoozeCount = intent.getIntExtra("snooze_count", 0)

            if (alarmId == -1) return

            // Run on main thread directly since we need to show UI immediately
            val alarm = runBlocking(Dispatchers.IO) {
                alarmRepository.getAlarmById(alarmId).firstOrNull()
            }

            alarm?.let {
                playAlarmSound(context, it)
                if (it.isVibrate) {
                    startVibration(context)
                }
                showFullScreenNotification(context, it, isRepeating, snoozeCount)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting alarm", e)
        } finally {
            wakeLock.release()
        }
    }

    private fun showFullScreenNotification(context: Context, alarm: Alarm, isRepeating: Boolean, snoozeCount: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(notificationManager)

        val notification = buildNotification(context, alarm, isRepeating, snoozeCount)
        
        try {
            notificationManager.notify(alarm.id, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm notifications"
                setSound(null, null) // Disable channel sound, we'll handle it manually
                enableVibration(false) // Disable channel vibration, we'll handle it manually
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(context: Context, alarm: Alarm, isRepeating: Boolean, snoozeCount: Int): Notification {
        val fullScreenIntent = createFullScreenIntent(context, alarm, isRepeating, snoozeCount)
        val stopIntent = createStopIntent(context, alarm.id)
        val snoozeIntent = createSnoozeIntent(context, alarm.id, isRepeating, snoozeCount)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(alarm.label.ifEmpty { "Alarm" })
            .setContentText(
                if (alarm.gameType != AlarmGameType.NONE)
                    "Complete the game to stop the alarm"
                else
                    "Time to wake up!"
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVibrate(null) // Disable notification vibration
            .setFullScreenIntent(fullScreenIntent, true)
            .setDefaults(NotificationCompat.DEFAULT_ALL and (NotificationCompat.DEFAULT_VIBRATE.inv())) // Disable default vibration
            .addAction(R.drawable.ic_stop, "Stop", stopIntent)
            .addAction(R.drawable.ic_snooze, "Snooze", snoozeIntent)
            .build()
            .apply {
                flags = flags or 
                    NotificationCompat.FLAG_INSISTENT or
                    NotificationCompat.FLAG_NO_CLEAR or
                    NotificationCompat.FLAG_FOREGROUND_SERVICE
            }
    }

    private fun createFullScreenIntent(context: Context, alarm: Alarm, isRepeating: Boolean, snoozeCount: Int): PendingIntent {
        val intent = if (alarm.gameType == AlarmGameType.NONE) {
            Intent(context, NoGameAlarmActivity::class.java)
        } else {
            Intent(context, AlarmGameActivity::class.java)
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("alarm_id", alarm.id)
            putExtra("game_type", alarm.gameType.ordinal)
            putExtra("is_preview", false)
            putExtra("is_repeating", isRepeating)
            putExtra("snooze_count", snoozeCount)
        }

        return PendingIntent.getActivity(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createStopIntent(context: Context, alarmId: Int): PendingIntent {
        val intent = Intent(context, StopReceiver::class.java).apply {
            putExtra("alarm_id", alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createSnoozeIntent(context: Context, alarmId: Int, isRepeating: Boolean, snoozeCount: Int): PendingIntent {
        val intent = Intent(context, SnoozeReceiver::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("is_repeating", isRepeating)
            putExtra("snooze_count", snoozeCount)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun playAlarmSound(context: Context, alarm: Alarm) {
        try {
            if (mediaPlayer == null) {
                val alarmSound = try {
                    if (alarm.soundUri.isNotEmpty()) {
                        alarm.soundUri.toUri()
                    } else {
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing sound URI, using default", e)
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                }

                Log.d(TAG, "Playing alarm with sound URI: $alarmSound")

                mediaPlayer = MediaPlayer().apply {
                    setOnPreparedListener { start() }
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                        false
                    }
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context, alarmSound)
                    isLooping = true
                    prepareAsync()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound", e)
            // Try to play default alarm sound as fallback
            try {
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context, defaultUri)
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing default alarm sound", e)
            }
        }
    }

    private fun startVibration(context: Context) {
        try {
            if (vibrator == null) {
                vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager =
                        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(VIBRATION_PATTERN, 0)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(VIBRATION_PATTERN, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting vibration", e)
        }
    }
}
