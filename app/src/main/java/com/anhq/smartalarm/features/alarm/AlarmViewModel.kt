package com.anhq.smartalarm.features.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhq.smartalarm.core.model.Alarm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlarmViewModel : ViewModel() {
    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms: StateFlow<List<Alarm>> = _alarms.asStateFlow()

    init {
        // Dữ liệu mẫu ban đầu
        _alarms.value = listOf(
            Alarm(
                id = 0,
                hour = 5,
                minute = 30,
                repeatDays = listOf(1, 2, 4, 5),
                label = "Morning Alarm",
                isEnabled = true,
                timeInMillis = System.currentTimeMillis() + 12 * 60 * 60 * 1000 + 28 * 60 * 1000,
                isVibrate = true,
            )
        )
    }

    // Hàm bật/tắt báo thức
    fun toggleAlarm(index: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            val currentAlarms = _alarms.value.toMutableList()
            if (index in currentAlarms.indices) {
                val updatedAlarm = currentAlarms[index].copy(isEnabled = isEnabled)
                currentAlarms[index] = updatedAlarm
                _alarms.value = currentAlarms
            }
        }
    }

    // Hàm thêm báo thức mới (có thể dùng khi tích hợp với màn hình thêm báo thức)
    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val currentAlarms = _alarms.value.toMutableList()
            currentAlarms.add(alarm)
            _alarms.value = currentAlarms
        }
    }

    // Hàm chỉnh sửa báo thức (có thể dùng khi tích hợp với màn hình chỉnh sửa)
    fun editAlarm(index: Int, updatedAlarm: Alarm) {
        viewModelScope.launch {
            val currentAlarms = _alarms.value.toMutableList()
            if (index in currentAlarms.indices) {
                currentAlarms[index] = updatedAlarm
                _alarms.value = currentAlarms
            }
        }
    }
}