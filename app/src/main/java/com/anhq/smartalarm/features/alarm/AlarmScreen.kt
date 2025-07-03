package com.anhq.smartalarm.features.alarm

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.anhq.smartalarm.core.designsystem.theme.headline1
import com.anhq.smartalarm.core.designsystem.theme.headline3
import com.anhq.smartalarm.core.designsystem.theme.label1
import com.anhq.smartalarm.core.designsystem.theme.title2
import com.anhq.smartalarm.core.designsystem.theme.title3
import com.anhq.smartalarm.core.model.Alarm
import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.core.model.DayOfWeek
import com.anhq.smartalarm.features.addalarm.navigation.navigateToAddAlarm
import com.anhq.smartalarm.features.editalarm.navigation.navigateToEditAlarm
import java.util.Locale

@Composable
fun AlarmRoute(
    navController: NavController,
) {
    val viewModel: AlarmViewModel = hiltViewModel()
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val selectedAlarms by viewModel.selectedAlarms.collectAsStateWithLifecycle()
    val showDeleteConfirmation by viewModel.showDeleteConfirmation.collectAsStateWithLifecycle()

    AlarmScreen(
        alarms = alarms,
        isSelectionMode = isSelectionMode,
        selectedAlarms = selectedAlarms,
        showDeleteConfirmation = showDeleteConfirmation,
        setAlarmActive = { alarm, isActive ->
            viewModel.updateAlarmStatus(alarm = alarm, isActive = isActive)
        },
        onEditClick = { alarmId ->
            navController.navigateToEditAlarm(id = alarmId)
        },
        onAddClick = {
            navController.navigateToAddAlarm()
        },
        onToggleSelectionMode = viewModel::toggleSelectionMode,
        onToggleAlarmSelection = viewModel::toggleAlarmSelection,
        onDeleteSelected = viewModel::showDeleteConfirmationDialog,
        onConfirmDelete = viewModel::confirmDeleteSelectedAlarms,
        onDismissDelete = viewModel::hideDeleteConfirmationDialog
    )
}

@Composable
fun AlarmScreen(
    alarms: List<Alarm>,
    isSelectionMode: Boolean,
    selectedAlarms: Set<Int>,
    showDeleteConfirmation: Boolean,
    setAlarmActive: (Alarm, Boolean) -> Unit,
    onEditClick: (Int) -> Unit,
    onAddClick: () -> Unit,
    onToggleSelectionMode: () -> Unit,
    onToggleAlarmSelection: (Int) -> Unit,
    onDeleteSelected: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit
) {
    Scaffold(
        modifier = Modifier.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
        topBar = {
            Text(
                text = "Báo thức",
                style = headline3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textAlign = TextAlign.Center
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có báo thức nào",
                        style = title2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        count = alarms.size,
                        key = { alarms[it].id }
                    ) { index ->
                        val alarm = alarms[index]
                        AlarmItem(
                            alarm = alarm,
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedAlarms.contains(alarm.id),
                            onAlarmClick = {
                                if (isSelectionMode) {
                                    onToggleAlarmSelection(it.id)
                                } else {
                                    onEditClick(it.id)
                                }
                            },
                            onAlarmLongClick = {
                                if (!isSelectionMode) {
                                    onToggleSelectionMode()
                                    onToggleAlarmSelection(it.id)
                                }
                            },
                            onAlarmToggle = { setAlarmActive(it, !it.isActive) }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                if (isSelectionMode) {
                    FloatingActionButton(
                        onClick = onToggleSelectionMode,
                        modifier = Modifier.padding(bottom = 8.dp),
                        shape = RoundedCornerShape(50.dp),
                        containerColor = Color.Magenta,
                        contentColor = Color.White
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Exit selection mode"
                        )
                    }
                }

                FloatingActionButton(
                    onClick = if (isSelectionMode) onDeleteSelected else onAddClick,
                    shape = RoundedCornerShape(50.dp),
                    containerColor = if (isSelectionMode) Color.Red else FloatingActionButtonDefaults.containerColor,
                ) {
                    Icon(
                        imageVector = if (isSelectionMode) Icons.Default.Delete else Icons.Default.Add,
                        contentDescription = if (isSelectionMode) "Delete selected" else "Add alarm"
                    )
                }
            }

            if (showDeleteConfirmation) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = onDismissDelete,
                    title = {
                        Text(
                            text = "Xác nhận xóa",
                            style = title2
                        )
                    },
                    text = {
                        Text(
                            text = "Bạn có chắc chắn muốn xóa ${selectedAlarms.size} báo thức đã chọn không?",
                            style = label1
                        )
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(
                            onClick = onConfirmDelete
                        ) {
                            Text(
                                text = "Xóa",
                                style = title3,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = onDismissDelete
                        ) {
                            Text(text = "Hủy", style = title3)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlarmItem(
    alarm: Alarm,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onAlarmClick: (Alarm) -> Unit,
    onAlarmLongClick: (Alarm) -> Unit,
    onAlarmToggle: (Alarm) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = if (alarm.isActive) {
        Brush.linearGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF9C27B0), Color(0xFF673AB7)))
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onAlarmClick(alarm) },
                onLongClick = { onAlarmLongClick(alarm) })
            .background(brush = gradient, shape = RoundedCornerShape(20.dp))
            .padding(16.dp)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Transparent),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = alarm.label, style = title2, color = Color.White
                )

                Text(
                    text = String.format(Locale.US, "%02d:%02d", alarm.hour, alarm.minute),
                    style = headline1,
                    color = Color.White
                )

                if (alarm.selectedDays.isNotEmpty()) {
                    Text(
                        text = alarm.getRepeatDaysString(),
                        style = title3,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Một lần",
                        style = title3,
                        color = Color.White
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected, onCheckedChange = { onAlarmClick(alarm) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            uncheckedColor = Color.White,
                            checkmarkColor = Color.Black
                        )
                    )
                }

                if (!isSelectionMode) {
                    Switch(
                        checked = alarm.isActive,
                        onCheckedChange = { onAlarmToggle(alarm) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFC847F4),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray
                        )
                    )
                }

                if (alarm.isActive) {
                    Text(
                        text = alarm.getCountdownTime(System.currentTimeMillis()),
                        style = label1,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = "Đang tắt",
                        style = label1,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewAlarmItem() {
    val sampleAlarm = Alarm(
        id = 1,
        hour = 7,
        minute = 30,
        label = "Morning Alarm",
        isActive = true,
        selectedDays = setOf(DayOfWeek.MON, DayOfWeek.WED, DayOfWeek.FRI),
        gameType = AlarmGameType.MATH_PROBLEM
    )

    MaterialTheme {
        AlarmScreen(
            alarms = listOf(sampleAlarm),
            isSelectionMode = false,
            selectedAlarms = emptySet(),
            setAlarmActive = { _, _ -> },
            onEditClick = {},
            onAddClick = {},
            onToggleSelectionMode = {},
            onToggleAlarmSelection = {},
            onDeleteSelected = {},
            onConfirmDelete = {},
            onDismissDelete = {},
            showDeleteConfirmation = false
        )
    }
}
