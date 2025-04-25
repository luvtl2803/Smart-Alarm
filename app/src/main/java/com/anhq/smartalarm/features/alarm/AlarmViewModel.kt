package com.anhq.smartalarm.features.alarm

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhq.smartalarm.core.data.repository.AlarmRepository
import com.anhq.smartalarm.core.model.Alarm
import com.anhq.smartalarm.core.utils.AlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    private val application = context.applicationContext as Application
    private val _permissionRequired = MutableLiveData(false)
    val permissionRequired: LiveData<Boolean> = _permissionRequired
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager


    val alarms = alarmRepository.getAlarms().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun updateAlarmStatus(alarm: Alarm, isActive: Boolean) {
        viewModelScope.launch {
            if (isActive) {
                setAlarm(alarm.id, alarm.timeInMillis)
            } else (
                    cancelAlarm(alarm.id)
                    )
            alarmRepository.updateAlarm(
                alarm = Alarm(
                    id = alarm.id,
                    hour = alarm.hour,
                    minute = alarm.minute,
                    timeInMillis = alarm.timeInMillis,
                    isActive = isActive,
                    label = alarm.label,
                    isVibrate = alarm.isVibrate,
                    repeatDays = alarm.repeatDays,
                )
            )
        }
    }

    private fun cancelAlarm(requestCode: Int) {
        try {
            val alarmIntent = Intent(application, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                application,
                requestCode,
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()

            Log.d(TAG, "Alarm canceled")
            showToast("Alarm canceled")
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling alarm", e)
            showToast("Failed to cancel alarm")
        }
    }

    private fun setAlarm(requestCode: Int, timeInMillis: Long) {
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
                timeInMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error setting alarm", e)
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
        private const val TAG = "AlarmViewModel"
    }
}