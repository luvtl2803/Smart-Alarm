package com.anhq.smartalarm.features.editalarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.anhq.smartalarm.core.data.repository.AlarmRepository
import com.anhq.smartalarm.core.model.Alarm
import com.anhq.smartalarm.core.utils.AlarmReceiver
import com.anhq.smartalarm.features.editalarm.navigation.EditAlarmRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EditAlarmViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmRepository: AlarmRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val application = context.applicationContext as Application
    val id = savedStateHandle.toRoute<EditAlarmRoute>().id
    private val _label = MutableStateFlow("Alarm Name")
    val label: StateFlow<String> = _label.asStateFlow()
    private val _hour = MutableStateFlow(0)
    val hour: StateFlow<Int> = _hour.asStateFlow()
    private val _minute = MutableStateFlow(0)
    val minute: StateFlow<Int> = _minute.asStateFlow()
    private val _repeatDays = MutableStateFlow(listOf<Int>())
    val repeatDays: StateFlow<List<Int>> = _repeatDays.asStateFlow()
    private val _isActive = MutableStateFlow(true)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    private val _isVibrate = MutableStateFlow(true)
    val isVibrate: StateFlow<Boolean> = _isVibrate.asStateFlow()
    private val _timeInMills = MutableStateFlow(0L)
    val timeInMills: StateFlow<Long> = _timeInMills.asStateFlow()


    private val _permissionRequired = MutableLiveData(false)
    val permissionRequired: LiveData<Boolean> = _permissionRequired
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val alarm = alarmRepository.getAlarmById(id).stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5_000),
        initialValue = null
    )

    init {
        viewModelScope.launch {
            alarm.filterNotNull().collect { alarm ->
                _label.value = alarm.label
                _hour.value = alarm.hour
                _minute.value = alarm.minute
                _repeatDays.value = alarm.repeatDays
                _isActive.value = alarm.isActive
                _isVibrate.value = alarm.isVibrate
                _timeInMills.value = alarm.timeInMillis
            }
        }
    }

    fun setLabel(label: String) {
        _label.value = label
    }
    fun setIsVibrate(isVibrate: Boolean) {
        _isVibrate.value = isVibrate
    }
    fun setRepeatDays(repeatDays: List<Int>) {
        _repeatDays.value = repeatDays
    }
    fun setTime(hour: Int, minute: Int) {
        _hour.value = hour
        _minute.value = minute
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        _timeInMills.value = calendar.timeInMillis
    }

    fun updateAlarm() {
        viewModelScope.launch {
            setAlarm(requestCode = id)
            val alarm = Alarm(
                id = id,
                label = label.value,
                hour = hour.value,
                minute = minute.value,
                repeatDays = repeatDays.value,
                isActive = true,
                isVibrate = isVibrate.value,
                timeInMillis = timeInMills.value
            )
            alarmRepository.updateAlarm(alarm)
        }
    }

    private fun setAlarm(requestCode: Int) {
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
                    _timeInMills.value,
                    pendingIntent
                )
                val timeStr =
                    formatTime(Calendar.getInstance().apply { timeInMillis = _timeInMills.value })
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
