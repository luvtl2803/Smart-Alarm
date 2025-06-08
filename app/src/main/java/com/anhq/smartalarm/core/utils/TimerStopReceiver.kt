package com.anhq.smartalarm.core.utils

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.anhq.smartalarm.core.data.repository.TimerRepository
import com.anhq.smartalarm.core.service.TimerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerStopReceiver : BroadcastReceiver() {
    @Inject
    lateinit var timerRepository: TimerRepository

    companion object {
        private const val TAG = "TimerStopReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getIntExtra("timer_id", -1)
        val shouldAddMinute = intent.getBooleanExtra("add_minute", false)

        if (timerId != -1) {
            Log.d(TAG, "Stopping timer $timerId")

            // Cancel notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(timerId)

            // Stop sound and vibration
            TimerReceiver.stopTimer()

            // Update timer state
            CoroutineScope(Dispatchers.IO).launch {
                timerRepository.getTimerById(timerId)?.let { timer ->
                    if (shouldAddMinute) {
                        // If adding a minute, let TimerService handle the update
                        val serviceIntent = Intent(context, TimerService::class.java).apply {
                            action = TimerService.ACTION_ADD_MINUTE
                            putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                        }
                        context.startService(serviceIntent)
                    } else {
                        // Reset timer to initial time and pause it
                        timerRepository.updateTimer(
                            timer.copy(
                                isRunning = true,
                                isPaused = true,
                                remainingTimeMillis = timer.currentInitialTimeMillis,
                                lastTickTime = System.currentTimeMillis()
                            )
                        )
                        
                        // Start service to show paused notification
                        val serviceIntent = Intent(context, TimerService::class.java)
                        context.startService(serviceIntent)
                    }
                }
            }
        }
    }
} 