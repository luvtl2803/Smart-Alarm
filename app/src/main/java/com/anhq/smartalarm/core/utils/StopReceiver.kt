package com.anhq.smartalarm.core.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.anhq.smartalarm.core.data.repository.AlarmHistoryRepository
import com.anhq.smartalarm.core.data.repository.AlarmRepository
import com.anhq.smartalarm.core.data.repository.AlarmSuggestionRepository
import com.anhq.smartalarm.core.model.DayOfWeek
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class StopReceiver : BroadcastReceiver() {
    @Inject
    lateinit var alarmRepository: AlarmRepository
    @Inject
    lateinit var alarmHistoryRepository: AlarmHistoryRepository
    @Inject
    lateinit var alarmSuggestionRepository: AlarmSuggestionRepository

    companion object {
        private const val TAG = "StopReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", -1)
        val triggeredAt = intent.getLongExtra("triggered_at", System.currentTimeMillis())
        
        if (alarmId == -1) {
            Log.e(TAG, "Invalid alarm ID")
            return
        }

        Log.d(TAG, "Stopping alarm $alarmId")

        AlarmReceiver.stopAlarm()
        NotificationManagerCompat.from(context).cancel(alarmId)

        CoroutineScope(Dispatchers.IO).launch {
            alarmHistoryRepository.recordAlarmHistory(
                alarmId = alarmId,
                userAction = "DISMISSED",
                triggeredAt = triggeredAt
            )
            
            val alarm = alarmRepository.getAlarmById(alarmId).firstOrNull()
            alarm?.let {
                if (it.selectedDays.isNotEmpty()) {
                    it.selectedDays.forEach { day ->
                        alarmSuggestionRepository.updateSuggestions(day)
                    }
                } else {
                    val calendar = Calendar.getInstance()
                    val calendarDay = calendar.get(Calendar.DAY_OF_WEEK)
                    val dayOfWeek = when (calendarDay) {
                        Calendar.MONDAY -> DayOfWeek.MON
                        Calendar.TUESDAY -> DayOfWeek.TUE
                        Calendar.WEDNESDAY -> DayOfWeek.WED
                        Calendar.THURSDAY -> DayOfWeek.THU
                        Calendar.FRIDAY -> DayOfWeek.FRI
                        Calendar.SATURDAY -> DayOfWeek.SAT
                        Calendar.SUNDAY -> DayOfWeek.SUN
                        else -> DayOfWeek.MON
                    }
                    alarmSuggestionRepository.updateSuggestions(dayOfWeek)
                }

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
