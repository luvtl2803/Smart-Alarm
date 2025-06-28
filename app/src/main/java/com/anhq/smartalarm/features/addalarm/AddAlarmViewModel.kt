package com.anhq.smartalarm.features.addalarm

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
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhq.smartalarm.core.data.repository.AlarmRepository
import com.anhq.smartalarm.core.data.repository.AlarmSuggestionRepository
import com.anhq.smartalarm.core.database.model.AlarmSuggestionEntity
import com.anhq.smartalarm.core.model.Alarm
import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.core.model.DayOfWeek
import com.anhq.smartalarm.core.utils.AlarmPreviewManager
import com.anhq.smartalarm.core.utils.AlarmReceiver
import com.anhq.smartalarm.core.utils.AlarmSound
import com.anhq.smartalarm.core.utils.AlarmSoundManager
import com.anhq.smartalarm.features.alarm.NoGameAlarmActivity
import com.anhq.smartalarm.features.game.AlarmGameActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddAlarmViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmRepository: AlarmRepository,
    private val alarmPreviewManager: AlarmPreviewManager,
    private val alarmSuggestionRepository: AlarmSuggestionRepository
) : ViewModel() {

    private val application = context.applicationContext as Application
    private val alarmSoundManager = AlarmSoundManager(context)
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val _label = MutableStateFlow("Báo thức")
    val label: StateFlow<String> = _label.asStateFlow()
    private val _selectedDays = MutableStateFlow<Set<DayOfWeek>>(emptySet())
    val selectedDays: StateFlow<Set<DayOfWeek>> = _selectedDays.asStateFlow()
    private val _isActive = MutableStateFlow(true)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    private val _isVibrate = MutableStateFlow(true)
    val isVibrate: StateFlow<Boolean> = _isVibrate.asStateFlow()
    private val _alarmSounds = MutableStateFlow<List<AlarmSound>>(emptyList())
    val alarmSounds: StateFlow<List<AlarmSound>> = _alarmSounds.asStateFlow()
    private val _selectedSound = MutableStateFlow<AlarmSound?>(null)
    val selectedSound: StateFlow<AlarmSound?> = _selectedSound.asStateFlow()
    private val _selectedTime = MutableStateFlow(LocalTime.now())
    val selectedTime: StateFlow<LocalTime> = _selectedTime.asStateFlow()
    private val _gameType = MutableStateFlow(AlarmGameType.NONE)
    val gameType: StateFlow<AlarmGameType> = _gameType.asStateFlow()

    private val _permissionRequired = MutableLiveData(false)
    val permissionRequired: LiveData<Boolean> = _permissionRequired

    private val _suggestions = MutableStateFlow<List<AlarmSuggestionEntity>>(emptyList())
    val suggestions: StateFlow<List<AlarmSuggestionEntity>> = _suggestions.asStateFlow()

    private val _showDuplicateDialog = MutableStateFlow<Alarm?>(null)
    val showDuplicateDialog = _showDuplicateDialog.asStateFlow()

    init {
        loadSystemAlarmSounds()
        checkAlarmPermission()
    }

    private fun loadSystemAlarmSounds() {
        val noSound = AlarmSound(
            uri = "".toUri(),
            title = "Im lặng"
        )

        val defaultAlarmSound = AlarmSound(
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
            title = getTitleFromUri(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        )
        _alarmSounds.value = listOf(noSound) + listOf(defaultAlarmSound) + alarmSoundManager.getAllAlarmSounds()
        _selectedSound.value = defaultAlarmSound
    }

    private fun getTitleFromUri(uri: Uri): String{
        return alarmSoundManager.getAlarmTitleFromUri(uri)
    }

    fun setLabel(label: String) {
        _label.value = label
    }

    fun setSelectedTime(time: LocalTime) {
        _selectedTime.value = time
    }

    fun setIsVibrate(isVibrate: Boolean) {
        _isVibrate.value = isVibrate
    }

    fun setAlarmSound(uri: Uri) {
        _selectedSound.value = AlarmSound(uri, getTitleFromUri(uri))
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

    fun toggleDay(day: DayOfWeek) {
        _selectedDays.value = if (_selectedDays.value.contains(day)) {
            _selectedDays.value - day
        } else {
            _selectedDays.value + day
        }
    }

    fun setGameType(type: AlarmGameType) {
        _gameType.value = type
    }

    private fun createAlarm(): Alarm {
        val soundUri = try {
            selectedSound.value?.uri?.toString() ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting sound URI, using default", e)
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()
        }
        Log.d(TAG, "Creating alarm with sound URI: $soundUri")

        return Alarm(
            hour = selectedTime.value.hour,
            minute = selectedTime.value.minute,
            isActive = isActive.value,
            isVibrate = isVibrate.value,
            selectedDays = selectedDays.value,
            label = label.value,
            gameType = gameType.value,
            soundUri = soundUri
        )
    }

    private suspend fun checkDuplicateAlarm(newAlarm: Alarm): Alarm? {
        return alarmRepository.getAllAlarms().first().find { existingAlarm ->
            existingAlarm.hour == newAlarm.hour &&
            existingAlarm.minute == newAlarm.minute &&
            existingAlarm.selectedDays == newAlarm.selectedDays
        }
    }

    fun saveAlarm(onSuccess: () -> Unit) {
        if (!checkAlarmPermission()) return

        viewModelScope.launch {
            val alarm = createAlarm()
            val duplicateAlarm = checkDuplicateAlarm(alarm)
            
            if (duplicateAlarm != null) {
                _showDuplicateDialog.value = duplicateAlarm
            } else {
                val id = alarmRepository.insertAlarm(alarm)
                scheduleAlarm(alarm.copy(id = id))
                onSuccess()
            }
        }
    }

    fun overwriteAlarm(onSuccess: () -> Unit) {
        if (!checkAlarmPermission()) return

        viewModelScope.launch {
            val newAlarm = createAlarm()
            val duplicateAlarm = _showDuplicateDialog.value

            if (duplicateAlarm != null) {
                // Update existing alarm with new settings
                val updatedAlarm = duplicateAlarm.copy(
                    label = newAlarm.label,
                    isActive = newAlarm.isActive,
                    isVibrate = newAlarm.isVibrate,
                    gameType = newAlarm.gameType,
                    soundUri = newAlarm.soundUri
                )
                alarmRepository.updateAlarm(updatedAlarm)
                scheduleAlarm(updatedAlarm)
                _showDuplicateDialog.value = null
                onSuccess()
            }
        }
    }

    fun dismissDuplicateDialog() {
        _showDuplicateDialog.value = null
    }

    private fun scheduleAlarm(alarm: Alarm) {
        if (!checkAlarmPermission()) return

        if (!alarm.isActive) return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis < System.currentTimeMillis() && alarm.selectedDays.isEmpty()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val alarmIntent = Intent(application, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
            putExtra("game_type", alarm.gameType.ordinal)
        }

        if (alarm.selectedDays.isEmpty()) {
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

            val timeStr = formatTime(calendar)
            Log.d(TAG, "Single alarm set: $timeStr with game type: ${alarm.gameType}")
            showToast("Đã đặt báo thức lúc $timeStr")
        } else {
            val daysString = alarm.selectedDays.joinToString(", ") { it.label }
            val timeStr = String.format(Locale.US, "%02d:%02d", alarm.hour, alarm.minute)
            
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

                Log.d(
                    TAG,
                    "Repeating alarm set: $timeStr (requestCode: $uniqueId) with game type: ${alarm.gameType}"
                )
            }

            showToast("Đã đặt báo thức lúc $timeStr vào các ngày: $daysString")
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

        if (daysUntilNext == 0 && calendar.timeInMillis <= System.currentTimeMillis()) {
            daysUntilNext = 7
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

    private fun formatTime(calendar: Calendar): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    }

    private fun showToast(message: String) {
        Toast.makeText(application, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCleared() {
        super.onCleared()
        stopPreview()
    }

    fun loadSuggestionsForDay(dayOfWeek: DayOfWeek) {
        viewModelScope.launch {
            alarmSuggestionRepository.updateSuggestions(dayOfWeek)

            alarmSuggestionRepository.getSuggestionsForDay(dayOfWeek)
                .collect { allSuggestions ->
                    val filteredSuggestions = if (selectedDays.value.isEmpty()) {
                        val currentTime = LocalTime.now()
                        allSuggestions.filter { suggestion ->
                            val suggestionTime = LocalTime.of(suggestion.hour, suggestion.minute)
                            suggestionTime.isAfter(currentTime)
                        }
                    } else {
                        allSuggestions
                    }
                    
                    _suggestions.value = filteredSuggestions
                }
        }
    }

    fun onSuggestionSelected(suggestion: AlarmSuggestionEntity, wasAccepted: Boolean) {
        viewModelScope.launch {
            alarmSuggestionRepository.markSuggestionUsed(suggestion, wasAccepted)
        }
    }

    companion object {
        private const val TAG = "AddAlarmViewModel"
    }
}