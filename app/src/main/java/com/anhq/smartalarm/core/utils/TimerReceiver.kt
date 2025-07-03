package com.anhq.smartalarm.core.utils

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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@AndroidEntryPoint
class TimerReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "timer_end_channel"
        private const val WAKE_LOCK_TIMEOUT = 30 * 1000L
        private val VIBRATION_PATTERN = longArrayOf(0, 1000, 1000)
        private const val TAG = "TimerReceiver"

        private var mediaPlayer: MediaPlayer? = null
        private var vibrator: Vibrator? = null
        private var vibrateJob: Job? = null
        private var updateJob: Job? = null
        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        fun stopTimer() {
            scope.launch {
                try {
                    updateJob?.cancelAndJoin()
                    vibrateJob?.cancelAndJoin()

                    mediaPlayer?.stopAndRelease()
                    mediaPlayer = null

                    vibrator?.cancel()
                    vibrator = null
                    vibrateJob = null
                    updateJob = null

                    Log.d(TAG, "Timer sound and vibration stopped successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping timer", e)
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "TimerReceiver.onReceive() called")

        val wakeLock = context.getSystemService<PowerManager>()?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SmartAlarm:TimerWakeLock"
        )?.apply {
            acquire(WAKE_LOCK_TIMEOUT)
        }

        scope.launch {
            try {
                val timerId = intent.getIntExtra("timer_id", -1)
                val soundUri = intent.getStringExtra("sound_uri")
                val isVibrate = intent.getBooleanExtra("is_vibrate", false)
                val endTime = System.currentTimeMillis()

                Log.d(TAG, "Timer ID: $timerId, isVibrate: $isVibrate")

                if (timerId == -1) {
                    Log.e(TAG, "Invalid timer ID")
                    wakeLock?.release()
                    return@launch
                }

                stopCurrentVibration()

                if (isVibrate) {
                    setupVibration(context)
                }

                withContext(Dispatchers.IO) {
                    setupSound(context, soundUri)
                }

                showFullScreenNotification(context, timerId, endTime)
                startNotificationUpdateJob(context, timerId, endTime)

            } catch (e: Exception) {
                Log.e(TAG, "Error in onReceive", e)
            } finally {
                wakeLock?.release()
            }
        }
    }

    private suspend fun stopCurrentVibration() {
        vibrator?.cancel()
        vibrateJob?.cancelAndJoin()
        vibrator = null
    }

    private fun setupVibration(context: Context) {
        Log.d(TAG, "Setting up vibration")
        vibrator = context.getVibrator()

        vibrateJob = scope.launch(Dispatchers.Default) {
            try {
                val effect = VibrationEffect.createWaveform(VIBRATION_PATTERN, 0)
                @Suppress("DEPRECATION")
                vibrator?.vibrate(
                    effect,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in vibration job", e)
            }
        }
    }

    private fun setupSound(context: Context, soundUri: String?) {
        if (soundUri.isNullOrEmpty()) return

        mediaPlayer?.stopAndRelease()

        try {
            setupCustomSound(context, soundUri)
        } catch (e: Exception) {
            e.printStackTrace()
            setupDefaultSound(context)
        }
    }

    private fun setupCustomSound(context: Context, soundUri: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, soundUri.toUri())
            setupAudioAttributes(AudioAttributes.USAGE_ALARM)
            isLooping = true
            prepare()
            start()
        }
    }

    private fun setupDefaultSound(context: Context) {
        try {
            mediaPlayer = MediaPlayer().apply {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                setDataSource(context, uri)
                setupAudioAttributes(AudioAttributes.USAGE_NOTIFICATION)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun startNotificationUpdateJob(context: Context, timerId: Int, endTime: Long) {
        updateJob?.cancelAndJoin()
        updateJob = scope.launch(Dispatchers.Main) {
            try {
                val notificationManager = context.getSystemService<NotificationManager>()
                while (isActive) {
                    val notification = buildNotification(context, timerId, endTime)
                    notificationManager?.notify(timerId, notification)
                    delay(1000)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in update job", e)
            }
        }
    }

    private fun showFullScreenNotification(context: Context, timerId: Int, endTime: Long) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(notificationManager)

        val notification = buildNotification(context, timerId, endTime)
        try {
            notificationManager.notify(timerId, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Hẹn giờ kết thúc",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Thông báo khi hẹn giờ kết thúc"
            setBypassDnd(true)
            enableLights(true)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(context: Context, timerId: Int, endTime: Long): Notification {
        val stopIntent = createStopIntent(context, timerId)
        val addMinuteIntent = createAddMinuteIntent(context, timerId)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Hẹn giờ đã đến!")
            .setContentText("Đã trôi qua: ${formatElapsedTime(System.currentTimeMillis() - endTime)}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSound(null)
            .setVibrate(null)
            .addAction(
                R.drawable.ic_add,
                "+1 phút",
                addMinuteIntent
            )
            .addAction(
                R.drawable.ic_stop,
                "Dừng",
                stopIntent
            )
            .setFullScreenIntent(null, true)
            .setShowWhen(true)
            .setWhen(endTime)
            .setOnlyAlertOnce(false)
            .setLocalOnly(true)
            .build()
    }

    private fun createStopIntent(context: Context, timerId: Int): PendingIntent {
        val intent = Intent(context, TimerStopReceiver::class.java).apply {
            putExtra("timer_id", timerId)
        }
        return PendingIntent.getBroadcast(
            context,
            timerId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createAddMinuteIntent(context: Context, timerId: Int): PendingIntent {
        val intent = Intent(context, TimerStopReceiver::class.java).apply {
            putExtra("timer_id", timerId)
            putExtra("add_minute", true)
        }
        return PendingIntent.getBroadcast(
            context,
            timerId + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatElapsedTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        return when {
            hours > 0 -> String.format(Locale.US,
                "%d giờ %02d phút %02d giây",
                hours,
                minutes % 60,
                seconds % 60
            )

            minutes > 0 -> String.format(Locale.US,"%d phút %02d giây", minutes, seconds % 60)
            else -> String.format(Locale.US,"%d giây", seconds)
        }
    }
}

// Extension functions
private fun Context.getVibrator(): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService<VibratorManager>()?.defaultVibrator
    } else {
        getSystemService<Vibrator>()
    }
}

private fun MediaPlayer.setupAudioAttributes(usage: Int) {
    setAudioAttributes(
        AudioAttributes.Builder()
            .setUsage(usage)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    )
}

private fun MediaPlayer.stopAndRelease() {
    if (isPlaying) {
        stop()
    }
    release()
}

private inline fun <reified T> Context.getSystemService(): T? {
    return getSystemServiceName(T::class.java)?.let { getSystemService(it) } as? T
}
