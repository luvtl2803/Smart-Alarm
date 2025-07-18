package com.anhq.smartalarm.features.editalarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhq.smartalarm.core.data.repository.AlarmRepository
import com.anhq.smartalarm.core.model.Alarm
import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.core.model.DayOfWeek
import com.anhq.smartalarm.core.utils.AlarmPreviewManager
import com.anhq.smartalarm.core.utils.AlarmReceiver
import com.anhq.smartalarm.core.utils.AlarmSound
import com.anhq.smartalarm.core.utils.AlarmSoundManager
import com.anhq.smartalarm.core.utils.SoundFileManager
import com.anhq.smartalarm.features.alarm.NoGameAlarmActivity
import com.anhq.smartalarm.features.game.AlarmGameActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class EditAlarmViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmRepository: AlarmRepository,
    private val alarmPreviewManager: AlarmPreviewManager,
    private val soundFileManager: SoundFileManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val application = context.applicationContext as Application
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val alarmSoundManager = AlarmSoundManager(context)

    private val alarmId: Int = checkNotNull(savedStateHandle["id"])

    private val _alarm = MutableStateFlow<Alarm?>(null)
    val alarm: StateFlow<Alarm?> = _alarm.asStateFlow()

    private val _selectedTime = MutableStateFlow<LocalTime>(LocalTime.now())
    val selectedTime: StateFlow<LocalTime> = _selectedTime.asStateFlow()

    private val _selectedDays = MutableStateFlow<Set<DayOfWeek>>(emptySet())
    val selectedDays: StateFlow<Set<DayOfWeek>> = _selectedDays.asStateFlow()

    private val _label = MutableStateFlow("")
    val label: StateFlow<String> = _label.asStateFlow()

    private val _isVibrate = MutableStateFlow(true)
    val isVibrate: StateFlow<Boolean> = _isVibrate.asStateFlow()

    private val _gameType = MutableStateFlow(AlarmGameType.NONE)
    val gameType: StateFlow<AlarmGameType> = _gameType.asStateFlow()

    private val _alarmSounds = MutableStateFlow<List<AlarmSound>>(emptyList())
    val alarmSounds: StateFlow<List<AlarmSound>> = _alarmSounds.asStateFlow()

    private val _selectedSound = MutableStateFlow<AlarmSound?>(null)
    val selectedSound: StateFlow<AlarmSound?> = _selectedSound.asStateFlow()

    private val _permissionRequired = MutableLiveData(false)
    val permissionRequired: LiveData<Boolean> = _permissionRequired

    init {
        loadAlarm()
        loadSystemAlarmSounds()
        checkAlarmPermission()
    }

    private fun loadAlarm() {
        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId).firstOrNull()
            alarm?.let {
                _alarm.value = it
                _selectedDays.value = it.selectedDays
                _label.value = it.label
                _isVibrate.value = it.isVibrate
                _gameType.value = it.gameType
                _selectedSound.value = AlarmSound(it.soundUri.toUri(), getTitleFromUri(it.soundUri.toUri()))
                _selectedTime.value = LocalTime.of(it.hour, it.minute)
            }
        }
    }

    private fun loadSystemAlarmSounds() {
        val noSound = AlarmSound(
            uri = "".toUri(),
            title = "Im lặng"
        )

        val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val defaultAlarmSound = AlarmSound(
            uri = defaultUri,
            title = alarmSoundManager.getAlarmTitleFromUri(defaultUri)
        )
        _alarmSounds.value = listOf(noSound) + listOf(defaultAlarmSound) + alarmSoundManager.getAllAlarmSounds()
    }

    private fun getTitleFromUri(uri: Uri): String{
        return alarmSoundManager.getAlarmTitleFromUri(uri)
    }

    fun setSelectedTime(time: LocalTime) {
        _selectedTime.value = time
    }

    fun toggleDay(day: DayOfWeek) {
        _selectedDays.value = if (_selectedDays.value.contains(day)) {
            _selectedDays.value - day
        } else {
            _selectedDays.value + day
        }
    }

    fun setLabel(label: String) {
        _label.value = label
    }

    fun setIsVibrate(isVibrate: Boolean) {
        _isVibrate.value = isVibrate
    }

    fun setGameType(type: AlarmGameType) {
        _gameType.value = type
    }

    fun setAlarmSound(soundUri: Uri) {
        Log.d("SoundUri", soundUri.toString())
        if (soundUri.toString().isNotEmpty()) {
            if (soundUri.toString().startsWith("content://com.android.providers")) {
                val internalUri = soundFileManager.copyAlarmSoundToInternal(soundUri)
                if (internalUri != null) {
                    _selectedSound.value = AlarmSound(
                        title = alarmSoundManager.getAlarmTitleFromUri(internalUri.toUri()),
                        uri = internalUri.toUri()
                    )
                }
            } else {
                _selectedSound.value = AlarmSound(
                    title = alarmSoundManager.getAlarmTitleFromUri(soundUri), uri = soundUri
                )
            }
        } else {
            soundFileManager.deleteAlarmSound()
            _selectedSound.value = AlarmSound(
                title = "Im lặng", uri = soundUri
            )
        }
    }

    fun previewAlarm(): Intent {
        alarmPreviewManager.startPreview(
            soundUri = selectedSound.value?.uri,
            isVibrate = isVibrate.value
        )

        return if (gameType.value == AlarmGameType.NONE) {
            Intent(context, NoGameAlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("is_preview", true)
            }
        } else {
            Intent(context, AlarmGameActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("game_type", gameType.value.ordinal)
                putExtra("is_preview", true)
            }
        }
    }

    fun stopPreview() {
        alarmPreviewManager.stopPreview()
    }

    fun updateAlarm() {
        viewModelScope.launch {
            val currentAlarm = _alarm.value ?: return@launch

            // Get the actual sound URI, either from selected sound or default
            val soundUri = selectedSound.value?.uri?.toString() ?: run {
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                defaultUri.toString()
            }

            val updatedAlarm = currentAlarm.copy(
                hour = selectedTime.value.hour,
                minute = selectedTime.value.minute,
                selectedDays = _selectedDays.value,
                label = _label.value,
                isVibrate = _isVibrate.value,
                gameType = _gameType.value,
                soundUri = soundUri,
                isActive = currentAlarm.isActive
            )
            alarmRepository.updateAlarm(updatedAlarm)
            if (updatedAlarm.isActive) {
                scheduleAlarm(updatedAlarm)
            } else {
                cancelExistingAlarms(updatedAlarm.id)
            }
        }
    }

    private fun scheduleAlarm(alarm: Alarm) {
        if (!checkAlarmPermission()) return

        // Cancel existing alarms
        cancelExistingAlarms(alarm.id)

        if (!alarm.isActive) return

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

    private fun cancelExistingAlarms(alarmId: Int) {
        // Cancel single alarm
        val intent = Intent(application, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            application,
            alarmId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }

        // Cancel repeating alarms
        DayOfWeek.entries.forEach { dayOfWeek ->
            val uniqueId = alarmId * 10 + dayOfWeek.ordinal
            val repeatingIntent = Intent(application, AlarmReceiver::class.java)
            val repeatingPendingIntent = PendingIntent.getBroadcast(
                application,
                uniqueId,
                repeatingIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            repeatingPendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
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
                _permissionRequired.value = true
                return false
            }
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

    override fun onCleared() {
        super.onCleared()
        stopPreview()
    }
}

