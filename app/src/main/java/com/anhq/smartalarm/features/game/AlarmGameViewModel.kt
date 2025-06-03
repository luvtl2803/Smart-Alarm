package com.anhq.smartalarm.features.game

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.anhq.smartalarm.core.game.AlarmGame
import com.anhq.smartalarm.core.game.AlarmGameFactory
import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.core.model.SettingsUiState
import com.anhq.smartalarm.core.sharereference.PreferenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmGameViewModel @Inject constructor(
    application: Application,
    private val preferenceHelper: PreferenceHelper,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    var currentGame: AlarmGame? by mutableStateOf(null)
        private set

    var gameType: AlarmGameType by mutableStateOf(AlarmGameType.NONE)
        private set

    var isPreview: Boolean by mutableStateOf(false)
        private set

    var alarmId: Int by mutableIntStateOf(-1)
        private set

    private var isRepeating: Boolean by mutableStateOf(false)
        private set

    var snoozeCount: Int by mutableIntStateOf(0)
        private set

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Load game type and other parameters from SavedStateHandle
        gameType = AlarmGameType.entries[savedStateHandle.get<Int>("game_type") ?: AlarmGameType.NONE.ordinal]
        isPreview = savedStateHandle.get<Boolean>("is_preview") ?: false
        alarmId = savedStateHandle.get<Int>("alarm_id") ?: -1
        isRepeating = savedStateHandle.get<Boolean>("is_repeating") ?: false
        snoozeCount = savedStateHandle.get<Int>("snooze_count") ?: 0

        // Load settings and create the game
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
                // Create the game with the retrieved difficulty
                currentGame = AlarmGameFactory.createGame(gameType, getApplication(), settings.gameDifficulty)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cleanup if needed
        when (val game = currentGame) {
            is com.anhq.smartalarm.core.game.ShakePhoneGame -> game.cleanup()
            else -> {}
        }
    }
}
