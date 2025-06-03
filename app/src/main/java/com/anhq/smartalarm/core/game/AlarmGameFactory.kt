package com.anhq.smartalarm.core.game

import android.content.Context
import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.core.model.GameDifficulty

object AlarmGameFactory {
    fun createGame(type: AlarmGameType, context: Context, difficulty: GameDifficulty): AlarmGame {
        return when (type) {
            AlarmGameType.MATH_PROBLEM -> MathProblemGame(difficulty)
            AlarmGameType.MEMORY_TILES -> MemoryTilesGame(difficulty)
            AlarmGameType.SHAKE_PHONE -> ShakePhoneGame(context, difficulty)
            AlarmGameType.NONE -> object : AlarmGame() {
                override val type = AlarmGameType.NONE
                override val title = "No Game"
                override val description = "Simple alarm"
                override fun reset() { isCompleted = false }
            }
        }
    }
}
