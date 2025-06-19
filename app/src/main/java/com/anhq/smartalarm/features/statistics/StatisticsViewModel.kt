package com.anhq.smartalarm.features.statistics

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhq.smartalarm.core.data.repository.EnhancedSleepData
import com.anhq.smartalarm.core.data.repository.SleepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import java.time.DayOfWeek
import java.time.ZoneId

data class StatisticsUiState(
    val sleepData: List<EnhancedSleepData> = emptyList(),
    val hasPermission: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val sleepRepository: SleepRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState(isLoading = true))
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        checkPermissionAndLoadData()
    }

    private fun checkPermissionAndLoadData() {
        viewModelScope.launch {
            try {
                val hasPermission = sleepRepository.hasPermission()
                _uiState.update { it.copy(hasPermission = hasPermission) }

                if (hasPermission) {
                    loadSleepData()
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    private fun loadSleepData() {
        viewModelScope.launch {
            try {
                sleepRepository.getEnhancedSleepData().collect { data ->
                    _uiState.update { 
                        it.copy(
                            sleepData = data,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun getPermissionIntent(): Intent = sleepRepository.getPermissionIntent()

    fun formatDuration(minutes: Long): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return String.format(Locale.US, "%dh %02dm", hours, mins)
    }

    fun formatDate(data: EnhancedSleepData): String {
        return data.date.format(DateTimeFormatter.ofPattern("dd/MM"))
    }

    fun formatDayOfWeek(data: EnhancedSleepData): String {
        return when (data.date.dayOfWeek) {
            DayOfWeek.MONDAY -> "Thứ hai"
            DayOfWeek.TUESDAY -> "Thứ ba"
            DayOfWeek.WEDNESDAY -> "Thứ tư"
            DayOfWeek.THURSDAY -> "Thứ năm"
            DayOfWeek.FRIDAY -> "Thứ sáu"
            DayOfWeek.SATURDAY -> "Thứ bảy"
            DayOfWeek.SUNDAY -> "Chủ nhật"
        }
    }

    fun formatTime(data: EnhancedSleepData): String {
        val startTime = data.startTime.atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
        val endTime = data.endTime.atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
        return "$startTime - $endTime"
    }

    fun formatActionInfo(data: EnhancedSleepData): String {
        if (data.alarmTriggerTime == null) return "Không có báo thức"

        val actionText = when (data.userAction) {
            "DISMISSED" -> "Tắt"
            "SNOOZED" -> "Báo lại"
            else -> "Không xác định"
        }

        val timeToActionMinutes = data.timeToAction?.let { it / (60 * 1000) } ?: 0
        val snoozeText = if (data.snoozeCount > 0) " (${data.snoozeCount} lần báo lại)" else ""

        return "$actionText sau ${timeToActionMinutes}p$snoozeText"
    }
}