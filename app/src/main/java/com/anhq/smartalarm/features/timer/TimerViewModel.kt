package com.anhq.smartalarm.features.timer

import android.app.Application
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anhq.smartalarm.core.data.repository.TimerRepository
import com.anhq.smartalarm.core.model.Timer
import com.anhq.smartalarm.core.service.TimerService
import com.anhq.smartalarm.core.utils.TimerReceiver
import com.anhq.smartalarm.core.utils.AlarmSound
import com.anhq.smartalarm.core.utils.AlarmSoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val timerRepository: TimerRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _timers = MutableStateFlow<List<Timer>>(emptyList())
    val timers = _timers.asStateFlow()

    private val _alarmSounds = MutableStateFlow<List<AlarmSound>>(emptyList())
    val alarmSounds = _alarmSounds.asStateFlow()

    private val alarmSoundManager = AlarmSoundManager(getApplication())

    private val runningTimers = timers.map { timers ->
        timers.filter { it.isRunning && !it.isPaused }
    }

    init {
        viewModelScope.launch {
            timerRepository.timers.collect { timers ->
                _timers.value = timers
            }
        }

        // Start countdown update loop
        viewModelScope.launch {
            while (true) {
                delay(1000) // Update every second
                updateRunningTimers()
            }
        }

        // Monitor running timers for service
        viewModelScope.launch {
            runningTimers.collect { timers ->
                val context = getApplication<Application>()
                if (timers.isNotEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(Intent(context, TimerService::class.java))
                    } else {
                        context.startService(Intent(context, TimerService::class.java))
                    }
                }
            }
        }

        loadSystemAlarmSounds()
    }

    private fun loadSystemAlarmSounds() {
        val noSound = AlarmSound(
            uri = "".toUri(),
            title = "Im lặng"
        )

        val defaultAlarmSound = AlarmSound(
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
            title = getTitleFromUri(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        )
        _alarmSounds.value = listOf(noSound) + listOf(defaultAlarmSound) + alarmSoundManager.getAllAlarmSounds()
    }

    private fun getTitleFromUri(uri: Uri): String {
        return alarmSoundManager.getAlarmTitleFromUri(uri)
    }

    private fun updateRunningTimers() {
        val now = System.currentTimeMillis()
        val updatedTimers = _timers.value.map { timer ->
            if (timer.isRunning && !timer.isPaused) {
                val elapsedTime = now - timer.lastTickTime
                val newRemainingTime = (timer.remainingTimeMillis - elapsedTime).coerceAtLeast(0)
                
                if (newRemainingTime == 0L) {
                    // Timer completed - Reset to initial time and pause
                    handleTimerComplete(timer)
                    timer.copy(
                        isRunning = true,
                        isPaused = true,
                        remainingTimeMillis = timer.currentInitialTimeMillis,
                        endedAt = now
                    )
                } else {
                    timer.copy(
                        remainingTimeMillis = newRemainingTime,
                        lastTickTime = now
                    )
                }
            } else {
                timer
            }
        }
        
        viewModelScope.launch {
            // Update all modified timers in database
            updatedTimers.forEach { timer ->
                val originalTimer = _timers.value.find { it.id == timer.id }
                if (originalTimer != timer) {
                    timerRepository.updateTimer(timer)
                }
            }
        }
        
        _timers.value = updatedTimers
        
        // Start service to show notification
        val context = getApplication<Application>()
        if (updatedTimers.any { it.isRunning && !it.isPaused }) {
            val serviceIntent = Intent(context, TimerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    private fun handleTimerComplete(timer: Timer) {
        val context = getApplication<Application>()
        val intent = Intent(context, TimerReceiver::class.java).apply {
            action = "com.anhq.smartalarm.TIMER_COMPLETE"
            putExtra("timer_id", timer.id)
            putExtra("sound_uri", timer.soundUri)
            putExtra("is_vibrate", timer.isVibrate)
        }
        context.sendBroadcast(intent)
    }

    fun addTimer(
        hours: Int,
        minutes: Int,
        seconds: Int,
        soundUri: String = "",
        isVibrate: Boolean = true
    ) {
        val totalMillis = ((hours * 60L + minutes) * 60L + seconds) * 1000L
        
        viewModelScope.launch {
            val timer = Timer(
                initialTimeMillis = totalMillis,
                remainingTimeMillis = totalMillis,
                soundUri = soundUri,
                isVibrate = isVibrate,
                isRunning = true,
                lastTickTime = System.currentTimeMillis()
            )
            timerRepository.addTimer(timer)

            // Start service immediately when adding a new timer
            val context = getApplication<Application>()
            val serviceIntent = Intent(context, TimerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    fun pauseTimer(timerId: Int) {
        viewModelScope.launch {
            timerRepository.getTimerById(timerId)?.let { timer ->
                val now = System.currentTimeMillis()
                val elapsedTime = now - timer.lastTickTime
                val currentRemainingTime = (timer.remainingTimeMillis - elapsedTime).coerceAtLeast(0)
                
                timerRepository.updateTimer(timer.copy(
                    isPaused = true,
                    remainingTimeMillis = currentRemainingTime,
                    lastTickTime = now
                ))
            }
        }
    }

    fun resumeTimer(timerId: Int) {
        viewModelScope.launch {
            timerRepository.getTimerById(timerId)?.let { timer ->
                val now = System.currentTimeMillis()
                val updatedTimer = timer.copy(
                    isPaused = false,
                    lastTickTime = now
                )
                timerRepository.updateTimer(updatedTimer)
            }
        }
    }

    fun stopTimer(timerId: Int) {
        viewModelScope.launch {
            timerRepository.deleteTimer(timerId)
        }
    }

    fun addOneMinute(timerId: Int) {
        viewModelScope.launch {
            timerRepository.getTimerById(timerId)?.let { timer ->
                val now = System.currentTimeMillis()
                val additionalTime = 60_000L // 1 minute in milliseconds
                
                // Calculate current remaining time
                val currentRemainingTime = if (timer.isRunning && !timer.isPaused) {
                    val elapsedTime = now - timer.lastTickTime
                    (timer.remainingTimeMillis - elapsedTime).coerceAtLeast(0)
                } else {
                    timer.remainingTimeMillis
                }
                
                // Add one minute to current remaining time
                val newRemainingTime = currentRemainingTime + additionalTime
                
                val newTimer = timer.copy(
                    currentInitialTimeMillis = timer.currentInitialTimeMillis + additionalTime,
                    remainingTimeMillis = newRemainingTime,
                    lastTickTime = now
                )
                
                timerRepository.updateTimer(newTimer)
            }
        }
    }

    fun resetTimer(timerId: Int) {
        viewModelScope.launch {
            timerRepository.getTimerById(timerId)?.let { timer ->
                val now = System.currentTimeMillis()
                val newTimer = timer.copy(
                    remainingTimeMillis = timer.currentInitialTimeMillis,
                    isRunning = true,
                    isPaused = false,
                    lastTickTime = now
                )
                timerRepository.updateTimer(newTimer)
            }
        }
    }
}