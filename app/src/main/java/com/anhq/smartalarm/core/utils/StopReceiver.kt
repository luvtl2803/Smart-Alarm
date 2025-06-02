package com.anhq.smartalarm.core.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.anhq.smartalarm.core.data.repository.AlarmRepository
import com.anhq.smartalarm.core.model.Alarm
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver to handle stopping the alarm and clearing related notifications.
 */
@AndroidEntryPoint
class StopReceiver : BroadcastReceiver() {
    @Inject
    lateinit var alarmRepository: AlarmRepository

    companion object {
        private const val TAG = "StopReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", -1)
        
        if (alarmId == -1) {
            Log.e(TAG, "Invalid alarm ID")
            return
        }

        Log.d(TAG, "Stopping alarm $alarmId")

        // Stop alarm sound and vibration
        AlarmReceiver.stopAlarm()

        // Cancel notification
        NotificationManagerCompat.from(context).cancel(alarmId)

        // Update alarm state
        CoroutineScope(Dispatchers.IO).launch {
            val alarm = alarmRepository.getAlarmById(alarmId).firstOrNull()
            alarm?.let {
                // Only disable alarms with no repeat days
                if (it.selectedDays.isEmpty()) {
                    Log.d(TAG, "Disabling non-repeating alarm $alarmId")
                    alarmRepository.updateAlarm(it.copy(isActive = false))
                } else {
                    Log.d(TAG, "Keeping repeating alarm $alarmId active with days: ${it.selectedDays}")
                }
            }
        }
    }
}
