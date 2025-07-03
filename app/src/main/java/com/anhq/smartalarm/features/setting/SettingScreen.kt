package com.anhq.smartalarm.features.setting

import android.annotation.SuppressLint
import android.app.Activity
import android.media.RingtoneManager
import android.net.Uri
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhq.smartalarm.R
import com.anhq.smartalarm.core.designsystem.theme.body2
import com.anhq.smartalarm.core.designsystem.theme.body4
import com.anhq.smartalarm.core.designsystem.theme.headline3
import com.anhq.smartalarm.core.designsystem.theme.label1
import com.anhq.smartalarm.core.designsystem.theme.title2
import com.anhq.smartalarm.core.designsystem.theme.title3
import com.anhq.smartalarm.core.model.GameDifficulty
import com.anhq.smartalarm.core.model.SettingsUiState
import com.anhq.smartalarm.core.model.ThemeOption
import com.anhq.smartalarm.core.utils.AlarmSound
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.util.Date

@SuppressLint("ContextCastToActivity")
@Composable
fun SettingRoute(
    viewModel: SettingViewModel = hiltViewModel()
) {
    val activity = LocalContext.current as Activity
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val alarmSounds by viewModel.alarmSounds.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    viewModel.handleSignInResult(
                        data = result.data,
                        onSuccess = {
                            scope.launch {
                                snackBarHostState.showSnackbar("Đăng nhập thành công")
                            }
                        },
                        onError = { exception ->
                            scope.launch {
                                snackBarHostState.showSnackbar(
                                    exception.message ?: "Đăng nhập thất bại"
                                )
                            }
                        }
                    )
                }

                Activity.RESULT_CANCELED -> {
                    scope.launch {
                        snackBarHostState.showSnackbar("Đăng nhập đã bị hủy")
                    }
                }
            }
        }
    )

    var showOverwriteDialog by remember { mutableStateOf(false) }
    var onConfirmOverwrite by remember { mutableStateOf<(() -> Unit)?>(null) }

    SettingScreen(
        uiState = uiState,
        alarmSounds = alarmSounds,
        snackBarHostState = snackBarHostState,
        onGoogleSignIn = {
            viewModel.signInWithGoogle(activity, launcher)
        },
        onSignOut = {
            viewModel.signOut()
            scope.launch {
                snackBarHostState.showSnackbar("Đã đăng xuất")
            }
        },
        onBackup = {
            viewModel.backupData(
                onSuccess = {
                    scope.launch {
                        snackBarHostState.showSnackbar("Đã sao lưu dữ liệu thành công")
                    }
                },
                onError = { error ->
                    scope.launch {
                        snackBarHostState.showSnackbar(error)
                    }
                },
                onExistingData = { onConfirm ->
                    showOverwriteDialog = true
                    onConfirmOverwrite = onConfirm
                }
            )
        },
        onSync = {
            viewModel.syncData(
                onSuccess = {
                    scope.launch {
                        snackBarHostState.showSnackbar("Đã đồng bộ dữ liệu thành công")
                    }
                },
                onError = { error ->
                    scope.launch {
                        snackBarHostState.showSnackbar(error)
                    }
                }
            )
        },
        onThemeChange = { theme ->
            viewModel.updateTheme(theme)
            viewModel.saveSettings()
            scope.launch {
                snackBarHostState.showSnackbar("Đã thay đổi chủ đề")
            }
        },
        onGameDifficultyChange = { difficulty ->
            viewModel.updateGameDifficulty(difficulty)
            viewModel.saveSettings()
            scope.launch {
                snackBarHostState.showSnackbar("Đã thay đổi độ khó game")
            }
        },
        onSnoozeDurationChange = { duration ->
            viewModel.updateSnoozeDuration(duration)
            viewModel.saveSettings()
        },
        onMaxSnoozeCountChange = { count ->
            viewModel.updateMaxSnoozeCount(count)
            viewModel.saveSettings()
        },
        onTimerSoundChange = { soundUri ->
            viewModel.updateTimerDefaultSoundUri(soundUri)
            viewModel.saveSettings()
            scope.launch {
                snackBarHostState.showSnackbar("Đã thay đổi âm thanh timer")
            }
        },
        onTimerVibrateChange = { isVibrate ->
            viewModel.updateTimerDefaultVibrate(isVibrate)
            viewModel.saveSettings()
            scope.launch {
                snackBarHostState.showSnackbar("Đã thay đổi chế độ rung")
            }
        },
        getAlarmTitleFromUri = { uri -> viewModel.getAlarmTitleFromUri(uri) }
    )

    if (showOverwriteDialog) {
        AlertDialog(
            onDismissRequest = { showOverwriteDialog = false },
            title = {
                Text(
                    text = "Xác nhận ghi đè",
                    style = title3,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Đã tồn tại bản sao lưu trên đám mây. Bạn có muốn ghi đè không?",
                    style = body2
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOverwriteDialog = false
                        onConfirmOverwrite?.invoke()
                    }
                ) {
                    Text(
                        text = "Ghi đè",
                        style = label1
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showOverwriteDialog = false }
                ) {
                    Text(
                        text = "Hủy",
                        style = label1
                    )
                }
            }
        )
    }
}

@Composable
fun SettingScreen(
    uiState: SettingsUiState,
    alarmSounds: List<AlarmSound>,
    snackBarHostState: SnackbarHostState,
    onGoogleSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onBackup: () -> Unit,
    onSync: () -> Unit,
    onThemeChange: (ThemeOption) -> Unit,
    onGameDifficultyChange: (GameDifficulty) -> Unit,
    onSnoozeDurationChange: (Int) -> Unit,
    onMaxSnoozeCountChange: (Int) -> Unit,
    onTimerSoundChange: (Uri) -> Unit,
    onTimerVibrateChange: (Boolean) -> Unit,
    getAlarmTitleFromUri: (String) -> String
) {
    var showSoundPicker by remember { mutableStateOf(false) }
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onTimerSoundChange(it) }
    }

    Scaffold(
        modifier = Modifier.padding(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        ),
        topBar = {
            Text(
                text = "Cá nhân hóa",
                style = headline3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textAlign = TextAlign.Center
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Account Settings
            AccountSection(
                user = uiState.user,
                lastBackupTime = uiState.lastBackupTime,
                onSignIn = onGoogleSignIn,
                onSignOut = onSignOut,
                onBackup = onBackup,
                onSync = onSync
            )

            // Theme Settings
            ThemeSection(
                currentTheme = uiState.theme,
                onThemeChange = onThemeChange
            )

            // Game Difficulty Settings
            GameDifficultySection(
                currentDifficulty = uiState.gameDifficulty,
                onDifficultyChange = onGameDifficultyChange
            )

            // Timer Settings
            TimerSection(
                defaultSoundUri = uiState.timerDefaultSoundUri,
                isVibrateEnabled = uiState.timerDefaultVibrate,
                onSoundClick = { showSoundPicker = true },
                onVibrateChange = onTimerVibrateChange,
                getAlarmTitleFromUri = getAlarmTitleFromUri
            )

            // Snooze Settings
            SnoozeSection(
                snoozeDuration = uiState.snoozeDurationMinutes,
                maxSnoozeCount = uiState.maxSnoozeCount,
                onDurationChange = onSnoozeDurationChange,
                onCountChange = onMaxSnoozeCountChange
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showSoundPicker) {
        SoundPickerDialog(
            currentSoundUri = uiState.timerDefaultSoundUri,
            alarmSounds = alarmSounds,
            onDismiss = { showSoundPicker = false },
            onSoundSelect = {
                onTimerSoundChange(it)
                showSoundPicker = false
            },
            onPickFromStorage = {
                fileLauncher.launch("audio/*")
                showSoundPicker = false
            }
        )
    }
}

@Composable
private fun AccountSection(
    user: FirebaseUser?,
    lastBackupTime: Long?,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onBackup: () -> Unit,
    onSync: () -> Unit
) {
    var showOverwriteDialog by remember { mutableStateOf(false) }
    val onConfirmOverwrite by remember { mutableStateOf<(() -> Unit)?>(null) }

    SettingsCard(
        title = "Sao lưu và đồng bộ",
        icon = painterResource(R.drawable.ic_cloud)
    ) {
        if (user != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // User Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.email ?: "Người dùng",
                        style = body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_google),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onSignOut) {
                        Text(
                            text = "Đăng xuất",
                            style = label1
                        )
                    }
                }

                // Backup Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hành động:",
                            style = body2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        lastBackupTime?.let {
                            Text(
                                text = "Lần cuối: ${
                                    DateFormat.getTimeFormat(
                                        LocalContext.current
                                    ).format(Date(it))
                                }",
                                style = body4,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                onBackup()
                            }
                        ) {
                            Text(
                                text = "Sao lưu",
                                style = label1
                            )
                        }
                        TextButton(onClick = onSync) {
                            Text(
                                text = "Đồng bộ",
                                style = label1
                            )
                        }
                    }
                }
            }
        } else {
            // Sign in button
            SettingsItem(
                title = "Đăng nhập để sao lưu",
                onClick = onSignIn,
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_google),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }

    if (showOverwriteDialog) {
        AlertDialog(
            onDismissRequest = { showOverwriteDialog = false },
            title = {
                Text(
                    text = "Xác nhận ghi đè",
                    style = title3,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Đã tồn tại bản sao lưu trên cloud. Bạn có muốn ghi đè không?",
                    style = body2
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOverwriteDialog = false
                        onConfirmOverwrite?.invoke()
                    }
                ) {
                    Text(
                        text = "Ghi đè",
                        style = label1
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showOverwriteDialog = false }
                ) {
                    Text(
                        text = "Hủy",
                        style = label1
                    )
                }
            }
        )
    }
}

@Composable
private fun ThemeSection(
    currentTheme: ThemeOption,
    onThemeChange: (ThemeOption) -> Unit
) {
    SettingsCard(
        title = "Chủ đề giao diện",
        icon = painterResource(R.drawable.ic_darkmode)
    ) {
        Column {
            ThemeOption.entries.forEach { theme ->
                SettingsItem(
                    title = when (theme) {
                        ThemeOption.LIGHT -> "Sáng"
                        ThemeOption.DARK -> "Tối"
                        ThemeOption.SYSTEM -> "Theo hệ thống"
                    },
                    onClick = { onThemeChange(theme) },
                    trailingIcon = {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun GameDifficultySection(
    currentDifficulty: GameDifficulty,
    onDifficultyChange: (GameDifficulty) -> Unit
) {
    SettingsCard(
        title = "Độ khó mặc định của game",
        icon = painterResource(R.drawable.ic_game)
    ) {
        Column {
            GameDifficulty.entries.forEach { difficulty ->
                SettingsItem(
                    title = when (difficulty) {
                        GameDifficulty.EASY -> "Dễ"
                        GameDifficulty.MEDIUM -> "Trung bình"
                        GameDifficulty.HARD -> "Khó"
                    },
                    subtitle = when (difficulty) {
                        GameDifficulty.EASY -> "Phù hợp cho người mới bắt đầu"
                        GameDifficulty.MEDIUM -> "Cân bằng giữa thử thách và khả năng"
                        GameDifficulty.HARD -> "Yêu cầu sự tập trung cao"
                    },
                    onClick = { onDifficultyChange(difficulty) },
                    trailingIcon = {
                        RadioButton(
                            selected = currentDifficulty == difficulty,
                            onClick = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun TimerSection(
    defaultSoundUri: String,
    isVibrateEnabled: Boolean,
    onSoundClick: () -> Unit,
    onVibrateChange: (Boolean) -> Unit,
    getAlarmTitleFromUri: (String) -> String
) {
    SettingsCard(
        title = "Cài đặt hẹn giờ",
        icon = painterResource(R.drawable.ic_timer)
    ) {
        Column {
            SettingsItem(
                title = "Âm thanh mặc định",
                subtitle = getAlarmTitleFromUri(defaultSoundUri),
                onClick = onSoundClick,
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_right),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            SettingsItem(
                title = "Rung khi hết giờ",
                subtitle = if (isVibrateEnabled) "Bật" else "Tắt",
                onClick = { onVibrateChange(!isVibrateEnabled) },
                trailingIcon = {
                    Switch(
                        checked = isVibrateEnabled,
                        onCheckedChange = onVibrateChange
                    )
                }
            )
        }
    }
}

@Composable
private fun SnoozeSection(
    snoozeDuration: Int,
    maxSnoozeCount: Int,
    onDurationChange: (Int) -> Unit,
    onCountChange: (Int) -> Unit
) {
    SettingsCard(
        title = "Cài đặt tạm hoãn",
        icon = painterResource(R.drawable.ic_snooze_clock)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Snooze Duration
            Column {
                Text(
                    "Thời gian tạm hoãn",
                    style = body2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "$snoozeDuration phút",
                    style = body4,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Slider(
                    value = snoozeDuration.toFloat(),
                    onValueChange = { onDurationChange(it.toInt()) },
                    valueRange = 1f..30f,
                    steps = 29,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Max Snooze Count
            Column {
                Text(
                    "Số lần tạm hoãn tối đa",
                    style = body2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "$maxSnoozeCount lần",
                    style = body4,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Slider(
                    value = maxSnoozeCount.toFloat(),
                    onValueChange = { onCountChange(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: Painter,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = title3,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = body2,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = body4,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailingIcon?.let {
            Box(modifier = Modifier.padding(start = 16.dp)) {
                it()
            }
        }
    }
}

@Composable
private fun SoundPickerDialog(
    currentSoundUri: String,
    alarmSounds: List<AlarmSound>,
    onDismiss: () -> Unit,
    onSoundSelect: (Uri) -> Unit,
    onPickFromStorage: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.height(500.dp),
        onDismissRequest = onDismiss,
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Âm thanh báo thức",
                style = title2,
                textAlign = TextAlign.Center
            )
        },
        text = {
            LazyColumn {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onPickFromStorage)
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_open_folder),
                            contentDescription = null
                        )
                        Text(
                            "Chọn từ bộ nhớ",
                            style = body2
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp
                    )
                }

                items(alarmSounds) { sound ->
                    SoundItem(
                        sound = sound,
                        isSelected = isSelectedSound(sound, currentSoundUri),
                        onClick = { onSoundSelect(sound.uri) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Đóng",
                    style = label1
                )
            }
        }
    )
}

@Composable
private fun SoundItem(
    sound: AlarmSound,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(
                    if (sound.uri.toString().isBlank()) R.drawable.ic_noti_off
                    else R.drawable.ic_noti_on
                ),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = sound.title,
                style = body2,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_check_circle),
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun isSelectedSound(sound: AlarmSound, currentUri: String): Boolean {
    val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()
    return when {
        currentUri.isEmpty() && sound.uri.toString().isEmpty() -> true
        currentUri == defaultUri && sound.uri.toString() == defaultUri -> true
        currentUri.startsWith("file://") &&
                sound.uri.toString().startsWith("content://") &&
                currentUri.endsWith(getFileName(sound.uri.toString())) -> true

        sound.uri.toString() == currentUri -> true
        else -> false
    }
}

private fun getFileName(uri: String): String {
    return try {
        uri.substringAfterLast("/")
    } catch (e: Exception) {
        ""
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun SettingScreenPreview() {
//    val previewUiState = SettingsUiState(
//        theme = ThemeOption.SYSTEM,
//        gameDifficulty = GameDifficulty.MEDIUM,
//        snoozeDurationMinutes = 5,
//        maxSnoozeCount = 3,
//        timerDefaultSoundUri = "",
//        timerDefaultVibrate = true
//    )
//
//    val snackBarHostState = remember { SnackbarHostState() }
//
//    SettingScreen(
//        uiState = previewUiState,
//        alarmSounds = emptyList(),
//        snackBarHostState = snackBarHostState,
//        onThemeChange = {},
//        onGameDifficultyChange = {},
//        onSnoozeDurationChange = {},
//        onMaxSnoozeCountChange = {},
//        onTimerSoundChange = {},
//        onTimerVibrateChange = {},
//        getAlarmTitleFromUri = { uri -> if (uri.isEmpty()) "Mặc định" else "Tùy chỉnh" }
//    )
//}
