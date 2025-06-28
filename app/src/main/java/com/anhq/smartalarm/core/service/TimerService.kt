package com.anhq.smartalarm.core.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.anhq.smartalarm.R
import com.anhq.smartalarm.app.MainActivity
import com.anhq.smartalarm.core.data.repository.TimerRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {
    @Inject
    lateinit var timerRepository: TimerRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var lastNotificationUpdate = 0L
    private var lastNotifiedTime = -1L
    private var lastRunningCount = -1

    private lateinit var updateJob: Job

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "timer_service_channel"
        private const val NOTIFICATION_ID = 1
        private const val UPDATE_INTERVAL = 1000L

        const val ACTION_PAUSE = "com.anhq.smartalarm.action.PAUSE"
        const val ACTION_RESUME = "com.anhq.smartalarm.action.RESUME"
        const val ACTION_ADD_MINUTE = "com.anhq.smartalarm.action.ADD_MINUTE"
        const val ACTION_RESET = "com.anhq.smartalarm.action.RESET"
        const val EXTRA_TIMER_ID = "timer_id"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> handlePauseAction(intent.getIntExtra(EXTRA_TIMER_ID, -1))
            ACTION_RESUME -> handleResumeAction(intent.getIntExtra(EXTRA_TIMER_ID, -1))
            ACTION_ADD_MINUTE -> handleAddMinuteAction(intent.getIntExtra(EXTRA_TIMER_ID, -1))
            ACTION_RESET -> handleResetAction(intent.getIntExtra(EXTRA_TIMER_ID, -1))
        }

        // Start monitoring timers if not already started
        if (!::updateJob.isInitialized || updateJob.isCancelled) {
            updateJob = serviceScope.launch {
                try {
                    while (true) {
                        val currentTime = System.currentTimeMillis()
                        val timers = timerRepository.timers.first()
                        val runningTimers = timers.filter { it.isRunning }

                        if (runningTimers.isEmpty()) {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            stopSelf()
                            break
                        }

                        val nearestTimer = runningTimers.minByOrNull { it.remainingTimeMillis }
                        val remainingTime = nearestTimer?.let {
                            if (it.isPaused) {
                                it.remainingTimeMillis
                            } else {
                                val elapsed = currentTime - it.lastTickTime
                                val remaining = (it.remainingTimeMillis - elapsed).coerceAtLeast(0)
                                if (remaining == 0L) {
                                    stopForeground(STOP_FOREGROUND_REMOVE)
                                    stopSelf()
                                    null
                                } else {
                                    remaining
                                }
                            }
                        } ?: 0

                        if (remainingTime > 0 && shouldUpdateNotification(runningTimers.size)) {
                            val notification = buildNotification(
                                runningTimers.size,
                                remainingTime,
                                nearestTimer?.id ?: -1,
                                nearestTimer?.isPaused ?: false
                            )
                            startForeground(NOTIFICATION_ID, notification)

                            lastNotificationUpdate = currentTime
                            lastNotifiedTime = remainingTime
                            lastRunningCount = runningTimers.size
                        }

                        delay(100)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }

    private fun handlePauseAction(timerId: Int) {
        if (timerId != -1) {
            serviceScope.launch {
                timerRepository.getTimerById(timerId)?.let { timer ->
                    val now = System.currentTimeMillis()
                    val elapsedTime = now - timer.lastTickTime
                    val currentRemainingTime =
                        (timer.remainingTimeMillis - elapsedTime).coerceAtLeast(0)
                    timerRepository.updateTimer(
                        timer.copy(
                            isPaused = true,
                            remainingTimeMillis = currentRemainingTime,
                            lastTickTime = now
                        )
                    )
                }
            }
        }
    }

    private fun handleResumeAction(timerId: Int) {
        if (timerId != -1) {
            serviceScope.launch {
                timerRepository.getTimerById(timerId)?.let { timer ->
                    timerRepository.updateTimer(
                        timer.copy(
                            isPaused = false,
                            lastTickTime = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    private fun handleAddMinuteAction(timerId: Int) {
        if (timerId != -1) {
            serviceScope.launch {
                timerRepository.getTimerById(timerId)?.let { timer ->
                    val now = System.currentTimeMillis()

                    if (timer.isRunning && !timer.isPaused && timer.remainingTimeMillis > 0) {
                        val elapsedTime = (now - timer.lastTickTime).coerceAtLeast(0)
                        val currentRemainingTime =
                            (timer.remainingTimeMillis - elapsedTime).coerceAtLeast(0)
                        val additionalTime = 60_000L
                        val newRemainingTime = currentRemainingTime + additionalTime

                        timerRepository.updateTimer(
                            timer.copy(
                                isRunning = true,
                                isPaused = false,
                                remainingTimeMillis = newRemainingTime,
                                currentInitialTimeMillis = timer.currentInitialTimeMillis + additionalTime,
                                lastTickTime = now
                            )
                        )
                    } else {
                        timerRepository.updateTimer(
                            timer.copy(
                                isRunning = true,
                                isPaused = false,
                                remainingTimeMillis = 60_000L,
                                currentInitialTimeMillis = 60_000L,
                                lastTickTime = now
                            )
                        )
                    }
                }
            }
        }
    }

    private fun handleResetAction(timerId: Int) {
        if (timerId != -1) {
            serviceScope.launch {
                timerRepository.getTimerById(timerId)?.let { timer ->
                    timerRepository.updateTimer(
                        timer.copy(
                            remainingTimeMillis = timer.currentInitialTimeMillis,
                            isPaused = true,
                            lastTickTime = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    private fun shouldUpdateNotification(
        currentRunningCount: Int
    ): Boolean {
        val currentTime = System.currentTimeMillis()

        if (lastRunningCount == -1 || currentRunningCount != lastRunningCount) {
            return true
        }

        if (currentTime - lastNotificationUpdate >= UPDATE_INTERVAL) {
            return true
        }

        return false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (::updateJob.isInitialized) {
            updateJob.cancel()
        }
        serviceJob.cancel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Hẹn giờ đang chạy",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Hiển thị thời gian còn lại của bộ hẹn giờ"
            setShowBadge(true)
            enableLights(false)
            enableVibration(false)
            setSound(null, null)
            lockscreenVisibility = NotificationCompat.VISIBILITY_SECRET
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(
        runningTimersCount: Int,
        remainingTime: Long,
        timerId: Int,
        isPaused: Boolean
    ) = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_alarm)
        .setContentTitle(
            when {
                runningTimersCount == 0 -> "Không có hẹn giờ nào đang chạy"
                isPaused -> "Hẹn giờ tạm dừng"
                else -> "Hẹn giờ đang chạy"
            }
        )
        .setContentText(
            when (runningTimersCount) {
                0 -> "Chạm để mở ứng dụng"
                1 -> formatTime(remainingTime)
                else -> "Gần nhất: ${formatTime(remainingTime)}"
            }
        )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .setVisibility(NotificationCompat.VISIBILITY_SECRET)
        .setOngoing(true)
        .setAutoCancel(false)
        .setShowWhen(false)
        .apply {
            if (runningTimersCount > 0) {
                if (isPaused) {
                    addAction(
                        NotificationCompat.Action(
                            R.drawable.ic_play,
                            "Tiếp tục",
                            createActionPendingIntent(ACTION_RESUME, timerId)
                        )
                    )
                    addAction(
                        NotificationCompat.Action(
                            R.drawable.ic_check_circle,
                            "Đặt lại",
                            createActionPendingIntent(ACTION_RESET, timerId)
                        )
                    )
                } else {
                    addAction(
                        NotificationCompat.Action(
                            R.drawable.ic_pause,
                            "Tạm dừng",
                            createActionPendingIntent(ACTION_PAUSE, timerId)
                        )
                    )
                    addAction(
                        NotificationCompat.Action(
                            R.drawable.ic_add,
                            "+1 phút",
                            createActionPendingIntent(ACTION_ADD_MINUTE, timerId)
                        )
                    )
                }
            }
        }
        .setContentIntent(createContentIntent())
        .build()

    private fun createActionPendingIntent(action: String, timerId: Int): PendingIntent {
        val intent = Intent(this, TimerService::class.java).apply {
            this.action = action
            putExtra(EXTRA_TIMER_ID, timerId)
        }
        return PendingIntent.getService(
            this,
            timerId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatTime(millis: Long): String {
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (millis % (1000 * 60)) / 1000
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }
} 