package com.anhq.smartalarm.features.timer

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anhq.smartalarm.core.data.repository.TimerRepository
import com.anhq.smartalarm.core.model.Timer
import com.anhq.smartalarm.core.service.TimerService
import com.anhq.smartalarm.core.sharereference.PreferenceHelper
import com.anhq.smartalarm.core.utils.TimerReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val timerRepository: TimerRepository,
    private val preferenceHelper: PreferenceHelper,
    application: Application
) : AndroidViewModel(application) {

    private val _timers = MutableStateFlow<List<Timer>>(emptyList())
    val timers = _timers.asStateFlow()

    private val runningTimers = timers.map { timers ->
        timers.filter { it.isRunning && !it.isPaused }
    }

    init {
        viewModelScope.launch {
            timerRepository.timers.collect { timers ->
                _timers.value = timers
            }
        }

        viewModelScope.launch {
            while (true) {
                delay(1000)
                updateRunningTimers()
            }
        }

        viewModelScope.launch {
            runningTimers.collect { timers ->
                val context = getApplication<Application>()
                if (timers.isNotEmpty()) {
                    context.startForegroundService(Intent(context, TimerService::class.java))
                }
            }
        }

    }

    private fun updateRunningTimers() {
        val now = System.currentTimeMillis()
        val completedTimers = mutableListOf<Timer>()
        
        val updatedTimers = _timers.value.map { timer ->
            if (timer.isRunning && !timer.isPaused) {
                val elapsedTime = now - timer.lastTickTime
                val newRemainingTime = (timer.remainingTimeMillis - elapsedTime).coerceAtLeast(0)

                if (newRemainingTime == 0L) {
                    completedTimers.add(timer)
                    timer.copy(
                        isRunning = true,
                        isPaused = true,
                        remainingTimeMillis = timer.currentInitialTimeMillis,
                        endedAt = now,
                        lastTickTime = now
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
            updatedTimers.forEach { timer ->
                val originalTimer = _timers.value.find { it.id == timer.id }
                if (originalTimer != timer) {
                    timerRepository.updateTimer(timer)
                }
            }
        }

        _timers.value = updatedTimers

        if (completedTimers.isNotEmpty()) {
            viewModelScope.launch {
                completedTimers.forEachIndexed { index, timer ->
                    delay(index * 500L)
                    handleTimerComplete(timer)
                }
            }
        }

        val context = getApplication<Application>()
        if (updatedTimers.any { it.isRunning && !it.isPaused }) {
            val serviceIntent = Intent(context, TimerService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }

    private fun handleTimerComplete(timer: Timer) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            val settings = preferenceHelper.settingsFlow.first()
            val intent = Intent(context, TimerReceiver::class.java).apply {
                action = "com.anhq.smartalarm.TIMER_COMPLETE"
                putExtra("timer_id", timer.id)
                putExtra("sound_uri", settings.timerDefaultSoundUri)
                putExtra("is_vibrate", settings.timerDefaultVibrate)
            }
            context.sendBroadcast(intent)
        }
    }

    fun addTimer(totalMillis: Long) {
        if (totalMillis < 1000L) {
            return
        }

        viewModelScope.launch {
            val timer = Timer(
                initialTimeMillis = totalMillis,
                remainingTimeMillis = totalMillis,
                isRunning = true,
                lastTickTime = System.currentTimeMillis()
            )
            timerRepository.addTimer(timer)

            val context = getApplication<Application>()
            val serviceIntent = Intent(context, TimerService::class.java)
            context.startForegroundService(serviceIntent)
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
                    isRunning = true,
                    remainingTimeMillis = if (timer.remainingTimeMillis == 0L) timer.currentInitialTimeMillis else timer.remainingTimeMillis,
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
                val elapsedTime = if (timer.isRunning && !timer.isPaused) {
                    (now - timer.lastTickTime).coerceAtLeast(0)
                } else {
                    0
                }
                val currentRemainingTime = (timer.remainingTimeMillis - elapsedTime).coerceAtLeast(0)
                val additionalTime = 60_000L
                val newRemainingTime = currentRemainingTime + additionalTime

                val newTimer = timer.copy(
                    currentInitialTimeMillis = timer.currentInitialTimeMillis + additionalTime,
                    remainingTimeMillis = newRemainingTime,
                    isRunning = true,
                    isPaused = false,
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