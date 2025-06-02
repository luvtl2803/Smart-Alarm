package com.anhq.smartalarm.core.game

import android.content.Context
import com.anhq.smartalarm.core.model.AlarmGameType

object AlarmGameFactory {
    fun createGame(type: AlarmGameType, context: Context): AlarmGame {
        return when (type) {
            AlarmGameType.MATH_PROBLEM -> MathProblemGame()
            AlarmGameType.MEMORY_TILES -> MemoryTilesGame()
            AlarmGameType.SHAKE_PHONE -> ShakePhoneGame(context)
            AlarmGameType.NONE -> object : AlarmGame() {
                override val type = AlarmGameType.NONE
                override val title = "No Game"
                override val description = "Simple alarm"
                override fun reset() { isCompleted = false }
            }
        }
    }
} 