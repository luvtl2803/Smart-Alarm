package com.anhq.smartalarm.features.setting.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anhq.smartalarm.features.setting.model.GameDifficulty
import com.anhq.smartalarm.features.setting.model.SettingsUiState
import com.anhq.smartalarm.features.setting.model.ThemeOption
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val THEME_KEY = intPreferencesKey("theme")
        val GAME_DIFFICULTY_KEY = intPreferencesKey("game_difficulty")
        val SNOOZE_DURATION_KEY = intPreferencesKey("snooze_duration")
        val MAX_SNOOZE_COUNT_KEY = intPreferencesKey("max_snooze_count")
    }

    val settingsFlow: Flow<SettingsUiState> = context.dataStore.data.map { preferences ->
        SettingsUiState(
            theme = ThemeOption.entries[
                preferences[PreferencesKeys.THEME_KEY] ?: ThemeOption.SYSTEM.ordinal
            ],
            gameDifficulty = GameDifficulty.entries[
                preferences[PreferencesKeys.GAME_DIFFICULTY_KEY] ?: GameDifficulty.MEDIUM.ordinal
            ],
            snoozeDurationMinutes = preferences[PreferencesKeys.SNOOZE_DURATION_KEY] ?: 5,
            maxSnoozeCount = preferences[PreferencesKeys.MAX_SNOOZE_COUNT_KEY] ?: 3
        )
    }

    suspend fun updateSettings(settings: SettingsUiState) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_KEY] = settings.theme.ordinal
            preferences[PreferencesKeys.GAME_DIFFICULTY_KEY] = settings.gameDifficulty.ordinal
            preferences[PreferencesKeys.SNOOZE_DURATION_KEY] = settings.snoozeDurationMinutes
            preferences[PreferencesKeys.MAX_SNOOZE_COUNT_KEY] = settings.maxSnoozeCount
        }
    }
} 