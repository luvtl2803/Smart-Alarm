package com.anhq.smartalarm.features.setting

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhq.smartalarm.R
import com.anhq.smartalarm.core.designsystem.theme.body2
import com.anhq.smartalarm.core.designsystem.theme.body4
import com.anhq.smartalarm.core.designsystem.theme.body5
import com.anhq.smartalarm.core.designsystem.theme.label1
import com.anhq.smartalarm.core.designsystem.theme.title3
import com.anhq.smartalarm.core.model.GameDifficulty
import com.anhq.smartalarm.core.model.SettingsUiState
import com.anhq.smartalarm.core.model.ThemeOption
import com.anhq.smartalarm.core.utils.AlarmSound
import kotlinx.coroutines.launch

@Composable
fun SettingRoute(
    viewModel: SettingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val alarmSounds by viewModel.alarmSounds.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    SettingScreen(
        uiState = uiState,
        alarmSounds = alarmSounds,
        snackBarHostState = snackBarHostState,
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
}

@Composable
fun SettingScreen(
    uiState: SettingsUiState,
    alarmSounds: List<AlarmSound>,
    snackBarHostState: SnackbarHostState,
    onThemeChange: (ThemeOption) -> Unit,
    onGameDifficultyChange: (GameDifficulty) -> Unit,
    onSnoozeDurationChange: (Int) -> Unit,
    onMaxSnoozeCountChange: (Int) -> Unit,
    onTimerSoundChange: (String) -> Unit,
    onTimerVibrateChange: (Boolean) -> Unit,
    getAlarmTitleFromUri: (String) -> String,
    modifier: Modifier = Modifier
) {
    var showSoundPicker by remember { mutableStateOf(false) }
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onTimerSoundChange(it.toString())
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Theme Settings
            SettingsCard(
                title = "Chủ đề giao diện",
                icon = painterResource(R.drawable.ic_darkmode)
            ) {
                ThemeOption.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onThemeChange(theme)
                            }
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (theme) {
                                ThemeOption.LIGHT -> "Sáng"
                                ThemeOption.DARK -> "Tối"
                                ThemeOption.SYSTEM -> "Theo hệ thống"
                            },
                            modifier = Modifier.weight(1f),
                            style = body2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        RadioButton(
                            selected = uiState.theme == theme,
                            onClick = { onThemeChange(theme) }
                        )
                    }
                }
            }

            // Game Difficulty Settings
            SettingsCard(
                title = "Độ khó mặc định của game",
                icon = painterResource(R.drawable.ic_game)
            ) {
                GameDifficulty.entries.forEach { difficulty ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onGameDifficultyChange(difficulty)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = when (difficulty) {
                                        GameDifficulty.EASY -> "Dễ"
                                        GameDifficulty.MEDIUM -> "Trung bình"
                                        GameDifficulty.HARD -> "Khó"
                                    },
                                    style = body2,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (difficulty) {
                                        GameDifficulty.EASY -> "Phù hợp cho người mới bắt đầu"
                                        GameDifficulty.MEDIUM -> "Cân bằng giữa thử thách và khả năng"
                                        GameDifficulty.HARD -> "Yêu cầu sự tập trung cao"
                                    },
                                    style = body4,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            RadioButton(
                                selected = uiState.gameDifficulty == difficulty,
                                onClick = { onGameDifficultyChange(difficulty) }
                            )
                        }
                    }
                }
            }

            // Timer Settings
            SettingsCard(
                title = "Cài đặt hẹn giờ",
                icon = painterResource(R.drawable.ic_timer)
            ) {
                // Timer Sound
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSoundPicker = true }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Âm thanh mặc định",
                            style = body2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getAlarmTitleFromUri(uiState.timerDefaultSoundUri),
                            style = body4,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_right),
                        contentDescription = "Chọn âm thanh",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Timer Vibration
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Rung khi hết giờ",
                            style = body2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.timerDefaultVibrate) "Bật" else "Tắt",
                            style = body4,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.timerDefaultVibrate,
                        onCheckedChange = { onTimerVibrateChange(it) }
                    )
                }
            }

            // Snooze Settings
            SettingsCard(
                title = "Cài đặt tạm hoãn",
                icon = painterResource(R.drawable.ic_snooze_clock)
            ) {
                // Snooze Duration
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Thời gian tạm hoãn",
                        style = body2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${uiState.snoozeDurationMinutes} phút",
                        style = body4,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = uiState.snoozeDurationMinutes.toFloat(),
                        onValueChange = { onSnoozeDurationChange(it.toInt()) },
                        valueRange = 1f..30f,
                        steps = 29,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Max Snooze Count
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)

                ) {
                    Text(
                        "Số lần tạm hoãn tối đa",
                        style = body2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${uiState.maxSnoozeCount} lần",
                        style = body4,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = uiState.maxSnoozeCount.toFloat(),
                        onValueChange = { onMaxSnoozeCountChange(it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (showSoundPicker) {
            AlertDialog(
                modifier = Modifier.height(500.dp),
                onDismissRequest = { showSoundPicker = false },
                title = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Âm thanh báo thức",
                        style = label1,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    LazyColumn {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        fileLauncher.launch("audio/*")
                                        showSoundPicker = false
                                    },
                                horizontalArrangement = Arrangement.spacedBy(15.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_open_folder),
                                    contentDescription = "Chọn từ thư viện"
                                )
                                Text(
                                    "Chọn từ bộ nhớ",
                                    style = body4
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(thickness = 2.dp)
                        }

                        items(alarmSounds) { sound ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onTimerSoundChange(sound.uri.toString())
                                        showSoundPicker = false
                                    }
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
                                            if (sound.uri.toString()
                                                    .isBlank()
                                            ) R.drawable.ic_noti_off
                                            else R.drawable.ic_noti_on
                                        ),
                                        contentDescription = "Selected"
                                    )
                                    Text(
                                        sound.title,
                                        style = body5
                                    )
                                }

                                if (uiState.timerDefaultSoundUri == sound.uri.toString()) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check_circle),
                                        contentDescription = "Selected"
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSoundPicker = false }) {
                        Text("Chọn")
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: Painter,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(title, style = title3, color = MaterialTheme.colorScheme.onSurface)
        }
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingScreenPreview() {
    val previewUiState = SettingsUiState(
        theme = ThemeOption.SYSTEM,
        gameDifficulty = GameDifficulty.MEDIUM,
        snoozeDurationMinutes = 5,
        maxSnoozeCount = 3,
        timerDefaultSoundUri = "",
        timerDefaultVibrate = true
    )

    val snackBarHostState = remember { SnackbarHostState() }

    SettingScreen(
        uiState = previewUiState,
        alarmSounds = emptyList(),
        snackBarHostState = snackBarHostState,
        onThemeChange = {},
        onGameDifficultyChange = {},
        onSnoozeDurationChange = {},
        onMaxSnoozeCountChange = {},
        onTimerSoundChange = {},
        onTimerVibrateChange = {},
        getAlarmTitleFromUri = { uri -> if (uri.isEmpty()) "Mặc định" else "Tùy chỉnh" }
    )
}
