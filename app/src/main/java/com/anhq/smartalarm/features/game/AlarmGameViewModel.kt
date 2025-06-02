package com.anhq.smartalarm.features.game

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.anhq.smartalarm.core.game.AlarmGame
import com.anhq.smartalarm.core.game.AlarmGameFactory
import com.anhq.smartalarm.core.model.AlarmGameType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlarmGameViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    
    var currentGame: AlarmGame? by mutableStateOf(null)
        private set

    var gameType: AlarmGameType by mutableStateOf(AlarmGameType.NONE)
        private set

    var isPreview: Boolean by mutableStateOf(false)
        private set

    var alarmId: Int by mutableStateOf(-1)
        private set

    var isRepeating: Boolean by mutableStateOf(false)
        private set

    var snoozeCount: Int by mutableStateOf(0)
        private set

    init {
        // Lấy thông tin từ SavedStateHandle
        gameType = AlarmGameType.entries[savedStateHandle.get<Int>("game_type") ?: AlarmGameType.NONE.ordinal]
        isPreview = savedStateHandle.get<Boolean>("is_preview") ?: false
        alarmId = savedStateHandle.get<Int>("alarm_id") ?: -1
        isRepeating = savedStateHandle.get<Boolean>("is_repeating") ?: false
        snoozeCount = savedStateHandle.get<Int>("snooze_count") ?: 0
        
        // Tạo game tương ứng
        currentGame = AlarmGameFactory.createGame(gameType, getApplication())
    }

    override fun onCleared() {
        super.onCleared()
        // Cleanup nếu cần
        when (val game = currentGame) {
            is com.anhq.smartalarm.core.game.ShakePhoneGame -> game.cleanup()
            else -> {}
        }
    }
} 