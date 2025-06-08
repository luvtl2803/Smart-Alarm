package com.anhq.smartalarm.core.data.repository

import com.anhq.smartalarm.core.database.dao.TimerDao
import com.anhq.smartalarm.core.database.model.TimerEntity
import com.anhq.smartalarm.core.model.Timer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerRepository @Inject constructor(
    private val timerDao: TimerDao
) {
    val timers: Flow<List<Timer>> = timerDao.getAllTimers().map { entities ->
        entities.map { it.toTimer() }
    }

    suspend fun getTimerById(id: Int): Timer? {
        return timerDao.getTimerById(id)?.toTimer()
    }

    suspend fun addTimer(timer: Timer) {
        timerDao.insertTimer(timer.toEntity())
    }

    suspend fun updateTimer(timer: Timer) {
        timerDao.updateTimer(timer.toEntity())
    }

    suspend fun deleteTimer(timerId: Int) {
        timerDao.deleteTimerById(timerId)
    }

    private fun TimerEntity.toTimer(): Timer {
        return Timer(
            id = id,
            initialTimeMillis = initialTimeMillis,
            currentInitialTimeMillis = currentInitialTimeMillis,
            remainingTimeMillis = remainingTimeMillis,
            lastTickTime = lastTickTime,
            isRunning = isRunning,
            isPaused = isPaused,
            soundUri = soundUri,
            isVibrate = isVibrate,
            endedAt = endedAt
        )
    }

    private fun Timer.toEntity(): TimerEntity {
        return TimerEntity(
            id = id,
            initialTimeMillis = initialTimeMillis,
            currentInitialTimeMillis = currentInitialTimeMillis,
            remainingTimeMillis = remainingTimeMillis,
            lastTickTime = lastTickTime,
            isRunning = isRunning,
            isPaused = isPaused,
            soundUri = soundUri,
            isVibrate = isVibrate,
            endedAt = endedAt
        )
    }
} 