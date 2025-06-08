package com.anhq.smartalarm.features.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhq.smartalarm.R
import com.anhq.smartalarm.core.designsystem.theme.SmartAlarmTheme
import com.anhq.smartalarm.core.designsystem.theme.headline3
import com.anhq.smartalarm.core.model.GameDifficulty
import com.anhq.smartalarm.core.model.SettingsUiState
import com.anhq.smartalarm.core.model.ThemeOption
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
        }
    )
}

@Composable
fun SettingScreen(
    uiState: SettingsUiState,
    snackBarHostState: SnackbarHostState,
    onThemeChange: (ThemeOption) -> Unit,
    onGameDifficultyChange: (GameDifficulty) -> Unit,
    onSnoozeDurationChange: (Int) -> Unit,
    onMaxSnoozeCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = Modifier.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
        topBar = {
            Text(
                text = "Cài đặt",
                style = MaterialTheme.typography.headline3,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Theme Settings
            SettingsCard(
                title = "Chủ đề giao diện",
                icon = painterResource(R.drawable.ic_darkmode)
            ) {
                ThemeOption.entries.forEach { theme ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = when (theme) {
                                    ThemeOption.LIGHT -> "Sáng"
                                    ThemeOption.DARK -> "Tối"
                                    ThemeOption.SYSTEM -> "Theo hệ thống"
                                }
                            )
                        },
                        trailingContent = {
                            RadioButton(
                                selected = uiState.theme == theme,
                                onClick = { onThemeChange(theme) }
                            )
                        }
                    )
                }
            }

            // Game Difficulty Settings
            SettingsCard(
                title = "Độ khó mặc định của game",
                icon = painterResource(R.drawable.ic_game)
            ) {
                GameDifficulty.entries.forEach { difficulty ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = when (difficulty) {
                                    GameDifficulty.EASY -> "Dễ"
                                    GameDifficulty.MEDIUM -> "Trung bình"
                                    GameDifficulty.HARD -> "Khó"
                                }
                            )
                        },
                        supportingContent = {
                            Text(
                                text = when (difficulty) {
                                    GameDifficulty.EASY -> "Phù hợp cho người mới bắt đầu"
                                    GameDifficulty.MEDIUM -> "Cân bằng giữa thử thách và khả năng"
                                    GameDifficulty.HARD -> "Yêu cầu sự tập trung cao"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        trailingContent = {
                            RadioButton(
                                selected = uiState.gameDifficulty == difficulty,
                                onClick = { onGameDifficultyChange(difficulty) }
                            )
                        }
                    )
                }
            }

            // Snooze Settings
            SettingsCard(
                title = "Cài đặt tạm hoãn",
                icon = painterResource(R.drawable.ic_snooze_clock)
            ) {
                // Snooze Duration
                ListItem(
                    headlineContent = { Text("Thời gian tạm hoãn") },
                    supportingContent = {
                        Column {
                            Text("${uiState.snoozeDurationMinutes} phút")
                            androidx.compose.material3.Slider(
                                value = uiState.snoozeDurationMinutes.toFloat(),
                                onValueChange = { onSnoozeDurationChange(it.toInt()) },
                                valueRange = 1f..30f,
                                steps = 29,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )
                        }
                    }
                )

                // Max Snooze Count
                ListItem(
                    headlineContent = { Text("Số lần tạm hoãn tối đa") },
                    supportingContent = {
                        Column {
                            Text("${uiState.maxSnoozeCount} lần")
                            androidx.compose.material3.Slider(
                                value = uiState.maxSnoozeCount.toFloat(),
                                onValueChange = { onMaxSnoozeCountChange(it.toInt()) },
                                valueRange = 1f..5f,
                                steps = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )
                        }
                    }
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
    ) {
        Column {
            ListItem(
                headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
                leadingContent = {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSettingScreen() {
    SmartAlarmTheme {
        SettingScreen(
            uiState = SettingsUiState(),
            snackBarHostState = remember { SnackbarHostState() },
            onThemeChange = {},
            onGameDifficultyChange = {},
            onSnoozeDurationChange = {},
            onMaxSnoozeCountChange = {}
        )
    }
}
