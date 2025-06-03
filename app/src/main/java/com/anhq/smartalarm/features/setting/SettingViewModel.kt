package com.anhq.smartalarm.features.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhq.smartalarm.core.designsystem.theme.ThemeManager
import com.anhq.smartalarm.core.model.GameDifficulty
import com.anhq.smartalarm.core.model.SettingsUiState
import com.anhq.smartalarm.core.model.ThemeOption
import com.anhq.smartalarm.core.sharereference.PreferenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val preferenceHelper: PreferenceHelper,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            preferenceHelper.settingsFlow.collect { settings ->
                _uiState.update {
                    it.copy(
                        theme = settings.theme,
                        gameDifficulty = settings.gameDifficulty,
                        snoozeDurationMinutes = settings.snoozeDurationMinutes,
                        maxSnoozeCount = settings.maxSnoozeCount,
                    )
                }
            }
        }
    }

    fun updateTheme(theme: ThemeOption) {
        _uiState.update { it.copy(theme = theme) }
        themeManager.updateTheme(theme)
    }

    fun updateGameDifficulty(difficulty: GameDifficulty) {
        _uiState.update { it.copy(gameDifficulty = difficulty)}
    }

    fun updateSnoozeDuration(minutes: Int) {
        _uiState.update { it.copy(snoozeDurationMinutes = minutes) }
    }

    fun updateMaxSnoozeCount(count: Int) {
        _uiState.update { it.copy(maxSnoozeCount = count) }
    }

    fun saveSettings() {
        viewModelScope.launch {
            preferenceHelper.updateSettings(uiState.value.copy())
            _uiState.update { it.copy() }
        }
    }
}


