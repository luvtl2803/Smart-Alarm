package com.anhq.smartalarm.features.statistics

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhq.smartalarm.core.data.repository.SleepData
import com.anhq.smartalarm.core.data.repository.SleepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class StatisticsUiState(
    val sleepData: List<SleepData> = emptyList(),
    val hasPermission: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val sleepRepository: SleepRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        checkPermissionAndLoadData()
    }

    private fun checkPermissionAndLoadData() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        val hasPermission = sleepRepository.hasPermission()
        _uiState.value = _uiState.value.copy(hasPermission = hasPermission)

        if (hasPermission) {
            loadSleepData()
        }
        _uiState.value = _uiState.value.copy(isLoading = false)
    }

    private fun loadSleepData() {
        viewModelScope.launch {
            sleepRepository.getSleepData().collect { data ->
                _uiState.value = _uiState.value.copy(sleepData = data)
            }
        }
    }

    fun getPermissionIntent(): Intent = sleepRepository.getPermissionIntent()

    fun formatDuration(minutes: Long): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return String.format("%dh %02dm", hours, mins)
    }


    fun formatDate(data: SleepData): String {
        return data.date.format(DateTimeFormatter.ofPattern("dd/MM"))
    }


    fun formatTime(data: SleepData): String {
        val startTime = data.startTime.atZone(java.time.ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
        val endTime = data.endTime.atZone(java.time.ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
        return "$startTime - $endTime"
    }
}