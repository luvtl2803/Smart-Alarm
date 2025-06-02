package com.anhq.smartalarm.features.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhq.smartalarm.features.setting.data.SettingDataStore
import com.anhq.smartalarm.features.setting.model.GameDifficulty
import com.anhq.smartalarm.features.setting.model.SettingsUiState
import com.anhq.smartalarm.features.setting.model.ThemeOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val settingDataStore: SettingDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingDataStore.settingsFlow.collect { settings ->
                _uiState.update { currentState ->
                    currentState.copy(
                        theme = settings.theme,
                        gameDifficulty = settings.gameDifficulty,
                        snoozeDurationMinutes = settings.snoozeDurationMinutes,
                        maxSnoozeCount = settings.maxSnoozeCount,
                        isDirty = false
                    )
                }
            }
        }
    }

    fun updateTheme(theme: ThemeOption) {
        _uiState.update { 
            it.copy(
                theme = theme,
                isDirty = true
            )
        }
    }

    fun updateGameDifficulty(difficulty: GameDifficulty) {
        _uiState.update { 
            it.copy(
                gameDifficulty = difficulty,
                isDirty = true
            )
        }
    }

    fun updateSnoozeDuration(minutes: Int) {
        _uiState.update { 
            it.copy(
                snoozeDurationMinutes = minutes,
                isDirty = true
            )
        }
    }

    fun updateMaxSnoozeCount(count: Int) {
        _uiState.update { 
            it.copy(
                maxSnoozeCount = count,
                isDirty = true
            )
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            settingDataStore.updateSettings(uiState.value.copy(isDirty = false))
            _uiState.update { it.copy(isDirty = false) }
        }
    }
}

