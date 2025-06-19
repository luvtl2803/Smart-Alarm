package com.anhq.smartalarm.core.data.repository

import java.time.Instant
import java.time.LocalDateTime

data class EnhancedSleepData(
    val date: LocalDateTime,
    val durationMinutes: Long,
    val startTime: Instant,
    val endTime: Instant,
    val alarmTriggerTime: Instant?,
    val userAction: String?,
    val timeToAction: Long?,
    val snoozeCount: Int
) 