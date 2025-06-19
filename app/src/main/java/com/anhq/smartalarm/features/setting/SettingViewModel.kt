package com.anhq.smartalarm.features.setting

import android.app.Application
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhq.smartalarm.core.designsystem.theme.ThemeManager
import com.anhq.smartalarm.core.model.GameDifficulty
import com.anhq.smartalarm.core.model.SettingsUiState
import com.anhq.smartalarm.core.model.ThemeOption
import com.anhq.smartalarm.core.sharereference.PreferenceHelper
import com.anhq.smartalarm.core.utils.AlarmSound
import com.anhq.smartalarm.core.utils.AlarmSoundManager
import com.anhq.smartalarm.core.utils.AlarmPreviewManager
import com.anhq.smartalarm.core.utils.SoundFileManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceHelper: PreferenceHelper,
    private val themeManager: ThemeManager,
    private val soundFileManager: SoundFileManager
) : ViewModel() {

    private val alarmSoundManager = AlarmSoundManager(context)
    private val alarmPreviewManager = AlarmPreviewManager(context)
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _alarmSounds = MutableStateFlow<List<AlarmSound>>(emptyList())
    val alarmSounds: StateFlow<List<AlarmSound>> = _alarmSounds.asStateFlow()

    init {
        loadSettings()
        loadSystemAlarmSounds()
    }

    private fun loadSystemAlarmSounds() {
        val noSound = AlarmSound(
            uri = "".toUri(),
            title = "Im lặng"
        )

        val defaultAlarmSound = AlarmSound(
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
            title = getAlarmTitleFromUri(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString())
        )
        _alarmSounds.value = listOf(noSound) + listOf(defaultAlarmSound) + alarmSoundManager.getAllAlarmSounds()
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
                        timerDefaultSoundUri = settings.timerDefaultSoundUri,
                        timerDefaultVibrate = settings.timerDefaultVibrate
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

    fun updateTimerDefaultSoundUri(soundUri: String) {
        if (soundUri.isNotEmpty()) {
            // Copy sound file to internal storage
            val internalUri = soundFileManager.copyTimerSoundToInternal(soundUri.toUri())
            if (internalUri != null) {
                _uiState.update { it.copy(timerDefaultSoundUri = internalUri) }
            }
        } else {
            // Delete existing sound file if any
            soundFileManager.deleteTimerSound()
            _uiState.update { it.copy(timerDefaultSoundUri = "") }
        }
    }

    fun updateTimerDefaultVibrate(isVibrate: Boolean) {
        _uiState.update { it.copy(timerDefaultVibrate = isVibrate) }
    }

    fun getAllAlarmSounds(): List<AlarmSound> {
        return alarmSoundManager.getAllAlarmSounds()
    }

    fun getAlarmTitleFromUri(uri: String): String {
        return if (uri.isNotEmpty()) {
            try {
                alarmSoundManager.getAlarmTitleFromUri(uri.toUri())
            } catch (e: Exception) {
                "Tùy chỉnh"
            }
        } else {
            "Mặc định"
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            preferenceHelper.updateSettings(uiState.value.copy())
            _uiState.update { it.copy() }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}


