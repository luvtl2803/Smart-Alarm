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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var alarmRepository: AlarmRepository

    companion object {
        private const val CHANNEL_ID = "alarm_notification_channel"
        private var mediaPlayer: MediaPlayer? = null
        private var vibrator: Vibrator? = null
        private val vibrationPattern = longArrayOf(0, 500, 500)
        private val vibrationAmplitudes = intArrayOf(0, 255, 0)
        private const val TAG = "AlarmReceiver"

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

    @SuppressLint("SuspiciousIndentation")
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "AlarmReceiver.onReceive() called")
        
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or 
            PowerManager.ACQUIRE_CAUSES_WAKEUP or 
            PowerManager.ON_AFTER_RELEASE,
            "SmartAlarm:AlarmWakeLock"
        ).apply {
            acquire(5 * 60 * 1000L)
        }

        try {
            val alarmId = intent.getIntExtra("alarm_id", -1)
            Log.d(TAG, "Processing alarm ID: $alarmId")

            if (alarmId == -1) {
                Log.e(TAG, "Invalid alarm ID")
                wakeLock.release()
                return
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                val alarm = alarmRepository.getAlarmById(alarmId).firstOrNull()
                    Log.d(TAG, "Retrieved alarm: $alarm")
                    
            alarm?.let {
                        withContext(Dispatchers.Main) {
                if (it.isVibrate) {
                                Log.d(TAG, "Starting vibration for alarm ${it.id}")
                    startVibration(context)
                }
                            playAlarmSound(context, it)
                            showFullScreenNotification(
                                context,
                                it,
                                isRepeating = intent.getBooleanExtra("is_repeating", false),
                                snoozeCount = intent.getIntExtra("snooze_count", 0)
                            )
            }
                    } ?: Log.e(TAG, "Alarm not found for ID: $alarmId")
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing alarm", e)
                } finally {
                    wakeLock.release()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onReceive", e)
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
            createFullScreenIntent(context, alarm, isRepeating, snoozeCount).send()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification or starting activity", e)
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Báo thức",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Thông báo báo thức"
            setBypassDnd(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(true)
            setSound(null, null)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(context: Context, alarm: Alarm, isRepeating: Boolean, snoozeCount: Int): Notification {
        val fullScreenIntent = createFullScreenIntent(context, alarm, isRepeating, snoozeCount)
        val stopIntent = createStopIntent(context, alarm.id)
        val snoozeIntent = createSnoozeIntent(context, alarm.id, isRepeating, snoozeCount)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(alarm.label.ifEmpty { "Báo thức" })
            .setContentText(
                if (alarm.gameType != AlarmGameType.NONE)
                    "Hoàn thành thử thách để tắt báo thức"
                else
                    "Đã đến giờ thức dậy!"
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSound(null)
            .setVibrate(null)
            .setFullScreenIntent(fullScreenIntent, true)
            .addAction(R.drawable.ic_snooze, "Tạm hoãn", snoozeIntent)
            .addAction(R.drawable.ic_stop, "Dừng", stopIntent)
            .build()
            .apply {
                flags = flags or 
                    NotificationCompat.FLAG_INSISTENT or
                    NotificationCompat.FLAG_NO_CLEAR or
                    NotificationCompat.FLAG_FOREGROUND_SERVICE or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Notification.FLAG_ONGOING_EVENT
                    } else {
                        0
                    }
            }
    }

    private fun createFullScreenIntent(context: Context, alarm: Alarm, isRepeating: Boolean, snoozeCount: Int): PendingIntent {
        val intent = if (alarm.gameType == AlarmGameType.NONE) {
            Intent(context, NoGameAlarmActivity::class.java)
        } else {
            Intent(context, AlarmGameActivity::class.java)
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
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
        val intent = Intent(context, AlarmStopReceiver::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("triggered_at", System.currentTimeMillis())
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createSnoozeIntent(context: Context, alarmId: Int, isRepeating: Boolean, snoozeCount: Int): PendingIntent {
        val intent = Intent(context, AlarmSnoozeReceiver::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("is_repeating", isRepeating)
            putExtra("snooze_count", snoozeCount)
            putExtra("triggered_at", System.currentTimeMillis())
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun startVibration(context: Context) {
        try {
            Log.d(TAG, "Getting vibrator service")
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                Log.d(TAG, "Using VibratorManager for Android 12+")
                vibratorManager.defaultVibrator.also {
                    Log.d(TAG, "Successfully got vibrator from VibratorManager: ${true}")
                }
            } else {
                Log.d(TAG, "Using legacy Vibrator service")
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val effect = VibrationEffect.createWaveform(
                vibrationPattern,
                vibrationAmplitudes, 0
            )
            Log.d(TAG, "Starting vibration with waveform effect")
            vibrator?.let {
                if (it.hasVibrator()) {
                    @Suppress("DEPRECATION")
                    it.vibrate(
                        effect, AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    )
                    Log.d(TAG, "Vibration started with waveform effect")
                } else {
                    Log.e(TAG, "Device does not have vibrator capability")
                }
            } ?: Log.e(TAG, "Vibrator is null when trying to start vibration")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting vibration", e)
        }
    }

    private fun playAlarmSound(context: Context, alarm: Alarm) {
        try {
            mediaPlayer?.release()
            mediaPlayer = null

            val alarmSound = try {
                if (alarm.soundUri.isNotEmpty()) {
                    val uri = alarm.soundUri.toUri()
                    if (uri.scheme == "file") {
                        val file = File(uri.path!!)
                        if (!file.exists()) {
                            Log.e(TAG, "Alarm sound file not found: ${uri.path}")
                            throw Exception("Sound file not found")
                        }
                    }
                    uri
                } else {
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing sound URI, using default", e)
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

            Log.d(TAG, "Playing alarm with sound URI: $alarmSound")

            mediaPlayer = MediaPlayer().apply {
                setOnPreparedListener {
                    Log.d(TAG, "MediaPlayer prepared, starting playback")
                    start()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    playDefaultSound(context)
                    true
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
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound", e)
            playDefaultSound(context)
        }
    }

    private fun playDefaultSound(context: Context) {
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
