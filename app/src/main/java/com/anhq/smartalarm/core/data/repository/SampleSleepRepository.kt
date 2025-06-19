package com.anhq.smartalarm.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleSleepRepository @Inject constructor() {
    private var sampleData = mutableListOf<SleepData>()

    fun addSampleSleepData(startTime: Long, endTime: Long) {
        val startDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(startTime),
            ZoneId.systemDefault()
        )
        
        sampleData.add(
            SleepData(
                date = startDateTime,
                durationMinutes = (endTime - startTime) / (60 * 1000),
                startTime = Instant.ofEpochMilli(startTime),
                endTime = Instant.ofEpochMilli(endTime)
            )
        )
    }

    fun getSleepData(): Flow<List<SleepData>> = flow {
        emit(sampleData.sortedBy { it.date })
    }

    fun clearData() {
        sampleData.clear()
    }
} 