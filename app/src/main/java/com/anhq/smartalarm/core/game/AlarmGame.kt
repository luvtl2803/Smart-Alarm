package com.anhq.smartalarm.core.game

import com.anhq.smartalarm.core.model.AlarmGameType

abstract class AlarmGame {
    abstract val type: AlarmGameType
    abstract val title: String
    abstract val description: String

    protected var isCompleted: Boolean = false

    var onGameComplete: (() -> Unit)? = null

    fun isGameCompleted() = isCompleted

    abstract fun reset()

    protected fun completeGame() {
        isCompleted = true
        onGameComplete?.invoke()
    }
} 