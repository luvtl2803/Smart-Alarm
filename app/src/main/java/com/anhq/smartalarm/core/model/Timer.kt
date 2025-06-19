package com.anhq.smartalarm.core.model

data class Timer(
    val id: Int = 0,
    val initialTimeMillis: Long, // Original time when timer was created
    val currentInitialTimeMillis: Long = initialTimeMillis, // Current initial time after adding minutes
    val remainingTimeMillis: Long,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val lastTickTime: Long = System.currentTimeMillis()
) 