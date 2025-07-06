package com.anhq.smartalarm.features.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhq.smartalarm.core.data.repository.AlarmRepository
import com.anhq.smartalarm.core.data.repository.TimerRepository
import com.anhq.smartalarm.core.designsystem.theme.ThemeManager
import com.anhq.smartalarm.core.firebase.FirebaseAuthHelper
import com.anhq.smartalarm.core.model.GameDifficulty
import com.anhq.smartalarm.core.model.SettingsUiState
import com.anhq.smartalarm.core.model.ThemeOption
import com.anhq.smartalarm.core.sharereference.PreferenceHelper
import com.anhq.smartalarm.core.utils.AlarmSound
import com.anhq.smartalarm.core.utils.AlarmSoundManager
import com.anhq.smartalarm.core.utils.SoundFileManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceHelper: PreferenceHelper,
    private val themeManager: ThemeManager,
    private val soundFileManager: SoundFileManager,
    private val alarmRepository: AlarmRepository,
    private val timerRepository: TimerRepository
) : ViewModel() {

    private val firebaseAuthHelper = FirebaseAuthHelper()
    private val alarmSoundManager = AlarmSoundManager(context)
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _alarmSounds = MutableStateFlow<List<AlarmSound>>(emptyList())
    val alarmSounds: StateFlow<List<AlarmSound>> = _alarmSounds.asStateFlow()

    init {
        updateUserState()
        loadSettings()
        loadSystemAlarmSounds()
    }

    private fun updateUserState() {
        _uiState.update {
            it.copy(user = firebaseAuthHelper.getCurrentUser())
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

        if (!preferenceHelper.hasDefaultTimerSound) {
            _uiState.update {
                it.copy(timerDefaultSoundUri = defaultAlarmSound.uri.toString())
            }
            preferenceHelper.hasDefaultTimerSound = true
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            preferenceHelper.settingsFlow.collect { settings ->
                _uiState.update {
                    it.copy(
                        theme = settings.theme,
                        gameDifficulty = settings.gameDifficulty,
                        snoozeDurationMinutes = settings.snoozeDurationMinutes,
                        maxSnoozeCount = settings.maxSnoozeCount,
                        timerDefaultSoundUri = settings.timerDefaultSoundUri,
                        timerDefaultVibrate = settings.timerDefaultVibrate
                    )
                }
            }
        }
    }

    fun updateTheme(theme: ThemeOption) {
        _uiState.update { it.copy(theme = theme) }
        themeManager.updateTheme(theme)
    }

    fun updateGameDifficulty(difficulty: GameDifficulty) {
        _uiState.update { it.copy(gameDifficulty = difficulty)}
    }

    fun updateSnoozeDuration(minutes: Int) {
        _uiState.update { it.copy(snoozeDurationMinutes = minutes) }
    }

    fun updateMaxSnoozeCount(count: Int) {
        _uiState.update { it.copy(maxSnoozeCount = count) }
    }

    fun updateTimerDefaultSoundUri(soundUri: Uri) {
        Log.d("SoundUri", soundUri.toString())
        if (soundUri.toString().isNotEmpty()) {
            if (soundUri.toString().startsWith("content://com.android.providers")) {
                val internalUri = soundFileManager.copyTimerSoundToInternal(soundUri)
                if (internalUri != null) {
                    _uiState.update { it.copy(timerDefaultSoundUri = internalUri) }
                    saveSettings()
                }
            } else {
                _uiState.update { it.copy(timerDefaultSoundUri = soundUri.toString()) }
                saveSettings()
            }
        } else {
            soundFileManager.deleteTimerSound()
            _uiState.update { it.copy(timerDefaultSoundUri = "") }
            saveSettings()
        }
    }

    fun updateTimerDefaultVibrate(isVibrate: Boolean) {
        _uiState.update { it.copy(timerDefaultVibrate = isVibrate) }
    }

    fun getAlarmTitleFromUri(uri: String): String {
        return when {
            uri.isEmpty() -> "Im lặng"
            uri == RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString() -> {
                alarmSoundManager.getAlarmTitleFromUri(uri.toUri())
            }

            else -> try {
                alarmSoundManager.getAlarmTitleFromUri(uri.toUri())
            } catch (e: Exception) {
                "Không xác định"
            }
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            preferenceHelper.updateSettings(uiState.value.copy())
            _uiState.update { it.copy() }
        }
    }

    fun signInWithGoogle(activity: Activity, launcher: ActivityResultLauncher<Intent>) {
        firebaseAuthHelper.initGoogleSignIn(activity)
        launcher.launch(firebaseAuthHelper.getSignInIntent())
    }

    fun handleSignInResult(data: Intent?, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                firebaseAuthHelper.handleSignInResult(
                    data = data,
                    onSuccess = {
                        updateUserState()
                        onSuccess()
                    },
                    onFailure = onError
                )
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun signOut() {
        firebaseAuthHelper.signOut()
        updateUserState()
    }

    private suspend fun checkExistingData(): Boolean {
        return try {
            firebaseAuthHelper.hasExistingData()
        } catch (e: Exception) {
            Log.e("SettingViewModel", "Error checking existing data", e)
            false
        }
    }

    private suspend fun backupWithOverwrite(onSuccess: () -> Unit, onError: (String) -> Unit) {
        try {
            firebaseAuthHelper.deleteAllData()

            val alarms = alarmRepository.getAllAlarms().first()
            val timers = timerRepository.timers.first()

            alarms.forEach { alarm ->
                firebaseAuthHelper.saveAlarmData(alarm)
            }

            timers.forEach { timer ->
                firebaseAuthHelper.saveTimerData(timer)
            }

            val currentTime = System.currentTimeMillis()
            _uiState.update {
                it.copy(lastBackupTime = currentTime)
            }

            onSuccess()
        } catch (e: Exception) {
            Log.e("SettingViewModel", "Error during backup with overwrite", e)
            onError(e.message ?: "Có lỗi xảy ra khi sao lưu dữ liệu")
        }
    }

    fun backupData(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onExistingData: (onConfirmOverwrite: () -> Unit) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (firebaseAuthHelper.getCurrentUser() == null) {
                    onError("Vui lòng đăng nhập để sao lưu dữ liệu")
                    return@launch
                }

                if (checkExistingData()) {
                    onExistingData {
                        viewModelScope.launch {
                            backupWithOverwrite(onSuccess, onError)
                        }
                    }
                } else {
                    val alarms = alarmRepository.getAllAlarms().first()
                    val timers = timerRepository.timers.first()

                    alarms.forEach { alarm ->
                        firebaseAuthHelper.saveAlarmData(alarm)
                    }

                    timers.forEach { timer ->
                        firebaseAuthHelper.saveTimerData(timer)
                    }

                    val currentTime = System.currentTimeMillis()
                    _uiState.update {
                        it.copy(lastBackupTime = currentTime)
                    }

                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SettingViewModel", "Error during backup process", e)
                onError(e.message ?: "Có lỗi xảy ra khi sao lưu dữ liệu")
            }
        }
    }

    fun syncData(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                if (firebaseAuthHelper.getCurrentUser() == null) {
                    onError("Vui lòng đăng nhập để đồng bộ dữ liệu")
                    return@launch
                }

                Log.d("SettingViewModel", "Starting sync process...")

                val cloudAlarms = firebaseAuthHelper.getAlarmData()
                val cloudTimers = firebaseAuthHelper.getTimerData()
                val localAlarms = alarmRepository.getAllAlarms().first()
                val localTimers = timerRepository.timers.first()

                Log.d("SettingViewModel", "Got ${cloudAlarms.size} alarms and ${cloudTimers.size} timers from cloud")

                try {
                    val newAlarms = cloudAlarms.filter { cloudAlarm ->
                        localAlarms.none { localAlarm ->
                            localAlarm.hour == cloudAlarm.hour &&
                            localAlarm.minute == cloudAlarm.minute &&
                            localAlarm.selectedDays == cloudAlarm.selectedDays
                        }
                    }

                    val newTimers = cloudTimers.filter { cloudTimer ->
                        localTimers.none { localTimer ->
                            localTimer.initialTimeMillis == cloudTimer.initialTimeMillis &&
                            localTimer.remainingTimeMillis == cloudTimer.remainingTimeMillis
                        }
                    }

                    newAlarms.forEach { alarm ->
                        alarmRepository.insertAlarm(alarm)
                        Log.d("SettingViewModel", "Synced new alarm with hour: ${alarm.hour}, minute: ${alarm.minute}")
                    }

                    newTimers.forEach { timer ->
                        timerRepository.addTimer(timer)
                        Log.d("SettingViewModel", "Synced new timer with initial time: ${timer.initialTimeMillis}")
                    }

                    Log.d("SettingViewModel", "Sync completed successfully. Added ${newAlarms.size} alarms and ${newTimers.size} timers")
                    onSuccess()
                } catch (e: Exception) {
                    Log.e("SettingViewModel", "Error during sync process", e)
                    onError(e.message ?: "Có lỗi xảy ra khi đồng bộ dữ liệu")
                }
            } catch (e: Exception) {
                Log.e("SettingViewModel", "Error collecting data", e)
                onError("Có lỗi xảy ra khi lấy dữ liệu từ Firebase")
            }
        }
    }
}
