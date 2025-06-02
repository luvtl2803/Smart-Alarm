package com.anhq.smartalarm.features.alarm

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.anhq.smartalarm.core.designsystem.theme.headline2
import com.anhq.smartalarm.core.designsystem.theme.headline3
import com.anhq.smartalarm.core.designsystem.theme.label1
import com.anhq.smartalarm.core.designsystem.theme.label2
import com.anhq.smartalarm.core.designsystem.theme.label3
import com.anhq.smartalarm.core.model.Alarm
import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.core.model.DayOfWeek
import com.anhq.smartalarm.features.addalarm.navigation.navigateToAddAlarm
import com.anhq.smartalarm.features.editalarm.navigation.navigateToEditAlarm

@Composable
fun AlarmRoute(
    navController: NavController,
) {
    val viewModel: AlarmViewModel = hiltViewModel()
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val selectedAlarms by viewModel.selectedAlarms.collectAsStateWithLifecycle()

    AlarmScreen(
        alarms = alarms,
        isSelectionMode = isSelectionMode,
        selectedAlarms = selectedAlarms,
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
        onDeleteSelected = viewModel::deleteSelectedAlarms
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    alarms: List<Alarm>,
    isSelectionMode: Boolean,
    selectedAlarms: Set<Int>,
    setAlarmActive: (Alarm, Boolean) -> Unit,
    onEditClick: (Int) -> Unit,
    onAddClick: () -> Unit,
    onToggleSelectionMode: () -> Unit,
    onToggleAlarmSelection: (Int) -> Unit,
    onDeleteSelected: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Báo Thức",
                        style = MaterialTheme.typography.headline3,

                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding), verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (alarms.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No alarms set",
                                style = MaterialTheme.typography.label1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(
                                count = alarms.size, key = { alarms[it].id }) { index ->
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
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                    Column(
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        if (isSelectionMode) {
                            FloatingActionButton(
                                onClick = onToggleSelectionMode,
                                modifier = Modifier
                                    .padding(10.dp),
                                shape = RoundedCornerShape(50.dp),
                                containerColor = Color.Magenta,
                                contentColor = Color.White,
                                content = {
                                    IconButton(onClick = onToggleSelectionMode) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Exit selection mode"
                                        )
                                    }
                                }
                            )
                        }
                        FloatingActionButton(
                            onClick = if (isSelectionMode) onDeleteSelected else onAddClick,
                            modifier = Modifier
                                .padding(10.dp),
                            shape = RoundedCornerShape(50.dp),
                            containerColor = if (isSelectionMode) Color.Red else Color.Black,
                            contentColor = Color.White,
                            content = {
                                if (isSelectionMode) {
                                    IconButton(onClick = onDeleteSelected) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete selected"
                                        )
                                    }
                                } else {
                                    Icon(
                                        Icons.Default.Add, contentDescription = "Add alarm"
                                    )
                                }
                            }
                        )
                    }

                }
            }
        })
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
            .background(brush = gradient, shape = RoundedCornerShape(20.dp))
            .padding(8.dp)
            .combinedClickable(
                onClick = { onAlarmClick(alarm) },
                onLongClick = { onAlarmLongClick(alarm) })
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(color = Color.Transparent),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = alarm.label, style = MaterialTheme.typography.label1, color = Color.White
                )

                Text(
                    text = String.format("%02d:%02d", alarm.hour, alarm.minute),
                    style = MaterialTheme.typography.headline2,
                    color = Color.White
                )

                Text(
                    text = alarm.getRepeatDaysString(),
                    style = MaterialTheme.typography.label2,
                    color = Color.White
                )
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
                        style = MaterialTheme.typography.label3,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = "Đang tắt",
                        style = MaterialTheme.typography.label3,
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
            onDeleteSelected = {})
    }
}
