package com.anhq.smartalarm.core.sharereference

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import androidx.core.content.edit
import com.anhq.smartalarm.core.model.GameDifficulty
import com.anhq.smartalarm.core.model.SettingsUiState
import com.anhq.smartalarm.core.model.ThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PreferenceHelper @Inject constructor(
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "app_shared_prefs"
        private const val IS_FIRST_RUN = "is_first_run"
        private const val KEY_THEME = "theme"
        private const val KEY_DIFFICULTY = "difficulty"
        private const val KEY_SNOOZE_DURATION = "snooze_duration"
        private const val KEY_MAX_SNOOZE_COUNT = "max_snooze_count"
    }

    val settingsFlow: Flow<SettingsUiState> = flow {
        emit(loadSettings())
    }

    private fun loadSettings(): SettingsUiState {
        return SettingsUiState(
            theme = ThemeOption.valueOf(
                prefs.getString(KEY_THEME, ThemeOption.SYSTEM.name) ?: ThemeOption.SYSTEM.name
            ),
            gameDifficulty = GameDifficulty.valueOf(
                prefs.getString(KEY_DIFFICULTY, GameDifficulty.MEDIUM.name) ?: GameDifficulty.MEDIUM.name
            ),
            snoozeDurationMinutes = prefs.getInt(KEY_SNOOZE_DURATION, 5),
            maxSnoozeCount = prefs.getInt(KEY_MAX_SNOOZE_COUNT, 3),
        )
    }

    fun updateSettings(settings: SettingsUiState) {
        prefs.edit {
            putString(KEY_THEME, settings.theme.name)
                .putString(KEY_DIFFICULTY, settings.gameDifficulty.name)
                .putInt(KEY_SNOOZE_DURATION, settings.snoozeDurationMinutes)
                .putInt(KEY_MAX_SNOOZE_COUNT, settings.maxSnoozeCount)
        }
    }

    fun getCurrentGameDifficulty(): GameDifficulty {
        return GameDifficulty.valueOf(
            prefs.getString(KEY_DIFFICULTY, GameDifficulty.MEDIUM.name) ?: GameDifficulty.MEDIUM.name
        )
    }


    var isFirstRun: Boolean
        get() = prefs.getBoolean(IS_FIRST_RUN, true)
        set(value) = prefs.edit { putBoolean(IS_FIRST_RUN, value) }


    fun clearAll() {
        prefs.edit { clear() }
    }
}
