package com.anhq.smartalarm.features.alarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhq.smartalarm.core.data.repository.AlarmRepository
import com.anhq.smartalarm.core.model.Alarm
import com.anhq.smartalarm.core.model.DayOfWeek
import com.anhq.smartalarm.core.utils.AlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    private val application = context.applicationContext as Application
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val alarms: StateFlow<List<Alarm>> = alarmRepository.getAllAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    private val _selectedAlarms = MutableStateFlow<Set<Int>>(emptySet())
    val selectedAlarms = _selectedAlarms.asStateFlow()

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation = _showDeleteConfirmation.asStateFlow()

    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) {
            _selectedAlarms.value = emptySet()
        }
    }

    fun toggleAlarmSelection(alarmId: Int) {
        _selectedAlarms.value = if (_selectedAlarms.value.contains(alarmId)) {
            _selectedAlarms.value - alarmId
        } else {
            _selectedAlarms.value + alarmId
        }
    }

    fun showDeleteConfirmationDialog() {
        _showDeleteConfirmation.value = true
    }

    fun hideDeleteConfirmationDialog() {
        _showDeleteConfirmation.value = false
    }

    fun confirmDeleteSelectedAlarms() {
        viewModelScope.launch {
            _selectedAlarms.value.forEach { alarmId ->
                alarms.value.find { it.id == alarmId }?.let { alarm ->
                    deleteAlarm(alarm)
                }
            }
            _selectedAlarms.value = emptySet()
            _isSelectionMode.value = false
            _showDeleteConfirmation.value = false
        }
    }

    fun updateAlarmStatus(alarm: Alarm, isActive: Boolean) {
        viewModelScope.launch {
            val updatedAlarm = alarm.copy(isActive = isActive)
            alarmRepository.updateAlarm(updatedAlarm)
            
            if (isActive) {
                scheduleAlarm(updatedAlarm)
            } else {
                cancelAlarm(alarm.id, alarm.selectedDays)
            }
        }
    }

    private fun scheduleAlarm(alarm: Alarm) {
        if (!checkAlarmPermission()) return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time has passed and no repeat days
        if (calendar.timeInMillis < System.currentTimeMillis() && alarm.selectedDays.isEmpty()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val alarmIntent = Intent(application, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
            putExtra("game_type", alarm.gameType.ordinal)
        }

        if (alarm.selectedDays.isEmpty()) {
            // Single alarm
            val pendingIntent = PendingIntent.getBroadcast(
                application,
                alarm.id,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                pendingIntent
            )
        } else {
            // Repeating alarms for each selected day
            alarm.selectedDays.forEach { dayOfWeek ->
                val nextAlarmTime = calculateNextAlarmTime(alarm.hour, alarm.minute, dayOfWeek)
                val uniqueId = alarm.id * 10 + dayOfWeek.ordinal
                val pendingIntent = PendingIntent.getBroadcast(
                    application,
                    uniqueId,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(nextAlarmTime, pendingIntent),
                    pendingIntent
                )
            }
        }
    }

    private fun calculateNextAlarmTime(hour: Int, minute: Int, dayOfWeek: DayOfWeek): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val targetDayOfWeek = when (dayOfWeek) {
            DayOfWeek.SUN -> Calendar.SUNDAY
            DayOfWeek.MON -> Calendar.MONDAY
            DayOfWeek.TUE -> Calendar.TUESDAY
            DayOfWeek.WED -> Calendar.WEDNESDAY
            DayOfWeek.THU -> Calendar.THURSDAY
            DayOfWeek.FRI -> Calendar.FRIDAY
            DayOfWeek.SAT -> Calendar.SATURDAY
        }

        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        var daysUntilNext = (targetDayOfWeek - currentDay + 7) % 7

        // If target is today but time has passed
        if (daysUntilNext == 0 && calendar.timeInMillis <= System.currentTimeMillis()) {
            daysUntilNext = 7 // Schedule for next week
        }

        calendar.add(Calendar.DAY_OF_MONTH, daysUntilNext)
        return calendar.timeInMillis
    }

    private fun checkAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    application,
                    "Please grant permission to schedule exact alarms",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                application.startActivity(intent)
                return false
            }
        }
        return true
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmRepository.deleteAlarm(alarm)
            cancelAlarm(alarm.id, alarm.selectedDays)
        }
    }

    private fun cancelAlarm(requestCode: Int, selectedDays: Set<DayOfWeek>) {
        val alarmIntent = Intent(application, AlarmReceiver::class.java)
        if (selectedDays.isEmpty()) {
            val pendingIntent = PendingIntent.getBroadcast(
                application,
                requestCode,
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
        } else {
            selectedDays.forEach { day ->
                val uniqueRequestCode = requestCode * 10 + day.ordinal
                val pendingIntent = PendingIntent.getBroadcast(
                    application,
                    uniqueRequestCode,
                    alarmIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    companion object {
        private const val TAG = "AlarmViewModel"
    }
}