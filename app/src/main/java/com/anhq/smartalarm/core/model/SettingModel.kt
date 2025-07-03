package com.anhq.smartalarm.core.model

import com.google.firebase.auth.FirebaseUser

data class SettingsUiState(
    val theme: ThemeOption = ThemeOption.SYSTEM,
    val gameDifficulty: GameDifficulty = GameDifficulty.MEDIUM,
    val snoozeDurationMinutes: Int = 5,
    val maxSnoozeCount: Int = 3,
    val timerDefaultSoundUri: String = "",
    val timerDefaultVibrate: Boolean = true,
    val user: FirebaseUser? = null,
    val lastBackupTime: Long? = null
)

enum class GameDifficulty {
    EASY, MEDIUM, HARD
}

enum class ThemeOption {
    LIGHT, DARK, SYSTEM
}
