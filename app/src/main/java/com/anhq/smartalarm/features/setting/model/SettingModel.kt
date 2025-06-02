package com.anhq.smartalarm.features.setting.model

data class SettingsUiState(
    val theme: ThemeOption = ThemeOption.SYSTEM,
    val gameDifficulty: GameDifficulty = GameDifficulty.MEDIUM,
    val snoozeDurationMinutes: Int = 5,
    val maxSnoozeCount: Int = 3,
    val isDirty: Boolean = false
)

enum class GameDifficulty {
    EASY, MEDIUM, HARD
}

enum class ThemeOption {
    LIGHT, DARK, SYSTEM
} 