package com.anhq.smartalarm.features.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhq.smartalarm.core.designsystem.theme.SmartAlarmTheme
import com.anhq.smartalarm.features.setting.model.GameDifficulty
import com.anhq.smartalarm.features.setting.model.SettingsUiState
import com.anhq.smartalarm.features.setting.model.ThemeOption
import kotlinx.coroutines.launch

@Composable
fun SettingRoute(
    viewModel: SettingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    SettingScreen(
        uiState = uiState,
        snackBarHostState = snackBarHostState,
        onThemeChange = viewModel::updateTheme,
        onGameDifficultyChange = viewModel::updateGameDifficulty,
        onSnoozeDurationChange = viewModel::updateSnoozeDuration,
        onMaxSnoozeCountChange = viewModel::updateMaxSnoozeCount,
        onSaveClick = {
            viewModel.saveSettings()
            scope.launch {
                snackBarHostState.showSnackbar("Đã lưu cài đặt")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    uiState: SettingsUiState,
    snackBarHostState: SnackbarHostState,
    onThemeChange: (ThemeOption) -> Unit,
    onGameDifficultyChange: (GameDifficulty) -> Unit,
    onSnoozeDurationChange: (Int) -> Unit,
    onMaxSnoozeCountChange: (Int) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Selection
            Text(
                text = "Chủ đề giao diện",
                style = MaterialTheme.typography.titleMedium
            )
            ThemeOption.entries.forEach { theme ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.theme == theme,
                        onClick = { onThemeChange(theme) }
                    )
                    Text(
                        text = when (theme) {
                            ThemeOption.LIGHT -> "Sáng"
                            ThemeOption.DARK -> "Tối"
                            ThemeOption.SYSTEM -> "Theo hệ thống"
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            HorizontalDivider()

            // Game Difficulty
            Text(
                text = "Độ khó mặc định của game",
                style = MaterialTheme.typography.titleMedium
            )
            GameDifficulty.entries.forEach { difficulty ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.gameDifficulty == difficulty,
                        onClick = { onGameDifficultyChange(difficulty) }
                    )
                    Text(
                        text = when (difficulty) {
                            GameDifficulty.EASY -> "Dễ"
                            GameDifficulty.MEDIUM -> "Trung bình"
                            GameDifficulty.HARD -> "Khó"
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            HorizontalDivider()

            // Snooze Settings
            Text(
                text = "Cài đặt tạm hoãn",
                style = MaterialTheme.typography.titleMedium
            )
            
            // Snooze Duration
            Column {
                Text(
                    text = "Thời gian tạm hoãn: ${uiState.snoozeDurationMinutes} phút",
                    modifier = Modifier.padding(vertical = 8.dp)
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
            Column {
                Text(
                    text = "Số lần tạm hoãn tối đa: ${uiState.maxSnoozeCount}",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Slider(
                    value = uiState.maxSnoozeCount.toFloat(),
                    onValueChange = { onMaxSnoozeCountChange(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Save Button
            if (uiState.isDirty) {
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Lưu thay đổi")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSettingScreen() {
    SmartAlarmTheme {
        SettingScreen(
            uiState = SettingsUiState(isDirty = true),
            snackBarHostState = remember { SnackbarHostState() },
            onThemeChange = {},
            onGameDifficultyChange = {},
            onSnoozeDurationChange = {},
            onMaxSnoozeCountChange = {},
            onSaveClick = {}
        )
    }
}
