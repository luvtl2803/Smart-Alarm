package com.anhq.smartalarm.features.addalarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhq.smartalarm.core.data.repository.AlarmRepository
import com.anhq.smartalarm.core.model.Alarm
import com.anhq.smartalarm.core.utils.AlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@HiltViewModel
class AddAlarmViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    private val application = context.applicationContext as Application

    private val _label = MutableStateFlow("Alarm Name")
    val label: StateFlow<String> = _label.asStateFlow()

    private val _repeatDays = MutableStateFlow(listOf<Int>())
    val repeatDays: StateFlow<List<Int>> = _repeatDays.asStateFlow()

    private val _isEnable = MutableStateFlow(true)
    val isEnable: StateFlow<Boolean> = _isEnable.asStateFlow()

    private val _isVibrate = MutableStateFlow(true)
    val isVibrate: StateFlow<Boolean> = _isVibrate.asStateFlow()

    private val _timeInMills = MutableStateFlow(0L)
    val timeInMills: StateFlow<Long> = _timeInMills.asStateFlow()

    private val _timePickerState = MutableStateFlow(
        TimePickerState(
            initialHour = 1,
            initialMinute = 1,
            is24Hour = true
        )
    )
    val timePickerState: StateFlow<TimePickerState> = _timePickerState.asStateFlow()

    private val _permissionRequired = MutableLiveData(false)
    val permissionRequired: LiveData<Boolean> = _permissionRequired
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun setLabel(label: String) {
        _label.value = label
    }

    fun getTimePickerState(timePickerState: TimePickerState) {
        _timePickerState.value = timePickerState
    }

    fun setIsVibrate(isVibrate: Boolean) {
        _isVibrate.value = isVibrate
    }

    fun setRepeatDays(repeatDays: List<Int>) {
        _repeatDays.value = repeatDays
    }

    fun setTimeInMills() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, _timePickerState.value.hour)
            set(Calendar.MINUTE, _timePickerState.value.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        _timeInMills.value = calendar.timeInMillis
    }

    fun saveAlarm() {
        viewModelScope.launch {
            val alarm = Alarm(
                id = 0,
                label = label.value,
                hour = timePickerState.value.hour,
                minute = timePickerState.value.minute,
                repeatDays = repeatDays.value,
                isActive = isEnable.value,
                isVibrate = isVibrate.value,
                timeInMillis = timeInMills.value
            )
            val id = alarmRepository.insertAlarm(alarm)
            setAlarm(id, timeInMills.value)
        }
    }

    private fun setAlarm(requestCode: Int, timeInMills: Long) {
        try {
            if (!checkAlarmPermission()) {
                return
            }

            val alarmIntent = Intent(application, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                application,
                requestCode,
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMills,
                pendingIntent
            )
            val timeStr =
                formatTime(Calendar.getInstance().apply { timeInMillis = timeInMills })
            Log.d(TAG, "Alarm set: $timeStr")
            showToast("Alarm set for $timeStr")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting alarm", e)
            showToast("Failed to set alarm")
        }
    }

    private fun checkAlarmPermission(): Boolean {
        if (!alarmManager.canScheduleExactAlarms()) {
            _permissionRequired.postValue(true)
            return false
        }
        return true
    }

    fun getExactAlarmPermissionIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        } else {
            null
        }
    }

    private fun formatTime(calendar: Calendar): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    }

    private fun showToast(message: String) {
        Toast.makeText(application, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "EditAlarmViewModel"
    }

}
