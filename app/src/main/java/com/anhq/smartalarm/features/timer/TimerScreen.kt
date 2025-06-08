package com.anhq.smartalarm.features.timer

import android.annotation.SuppressLint
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anhq.smartalarm.R
import com.anhq.smartalarm.core.designsystem.theme.headline3
import com.anhq.smartalarm.core.model.Timer
import com.anhq.smartalarm.core.utils.AlarmSound
import java.util.concurrent.TimeUnit

@Composable
fun TimerRoute(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val timers by viewModel.timers.collectAsState()
    val alarmSounds by viewModel.alarmSounds.collectAsState()

    TimerScreen(
        timers = timers,
        alarmSounds = alarmSounds,
        onAddTimer = viewModel::addTimer,
        onPauseTimer = viewModel::pauseTimer,
        onResumeTimer = viewModel::resumeTimer,
        onStopTimer = viewModel::stopTimer,
        onAddOneMinute = viewModel::addOneMinute,
        onResetTimer = viewModel::resetTimer
    )
}

@Composable
fun TimerScreen(
    timers: List<Timer>,
    alarmSounds: List<AlarmSound>,
    onAddTimer: (Int, Int, Int, String, Boolean) -> Unit,
    onPauseTimer: (Int) -> Unit,
    onResumeTimer: (Int) -> Unit,
    onStopTimer: (Int) -> Unit,
    onAddOneMinute: (Int) -> Unit,
    onResetTimer: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.padding(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        ),
        topBar = {
            Text(
                text = "Hẹn giờ",
                style = MaterialTheme.typography.headline3,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm hẹn giờ")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (timers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có hẹn giờ nào",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(timers) { timer ->
                        TimerCard(
                            timer = timer,
                            onPause = { onPauseTimer(timer.id) },
                            onResume = { onResumeTimer(timer.id) },
                            onStop = { onStopTimer(timer.id) },
                            onAddMinute = { onAddOneMinute(timer.id) },
                            onReset = { onResetTimer(timer.id) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddTimerDialog(
                alarmSounds = alarmSounds,
                onDismiss = { showAddDialog = false },
                onConfirm = { hours, minutes, seconds, soundUri, isVibrate ->
                    onAddTimer(hours, minutes, seconds, soundUri, isVibrate)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun TimerCard(
    timer: Timer,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onAddMinute: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(timer.remainingTimeMillis),
                    style = MaterialTheme.typography.headlineMedium
                )
                Row {
                    IconButton(
                        onClick = if (timer.isPaused) onResume else onPause
                    ) {
                        Icon(
                            painter = painterResource(if (timer.isPaused) R.drawable.ic_play else R.drawable.ic_pause),
                            contentDescription = if (timer.isPaused) "Tiếp tục" else "Tạm dừng"
                        )
                    }
                    IconButton(onClick = onStop) {
                        Icon(
                            painter = painterResource(R.drawable.ic_stop),
                            contentDescription = "Dừng"
                        )
                    }
                }
            }


            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddMinute,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+1 phút")
                }

                Button(
                    onClick = onReset,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Đặt lại")
                }
            }
        }
    }
}

@Composable
fun AddTimerDialog(
    alarmSounds: List<AlarmSound>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Int, String, Boolean) -> Unit
) {
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(0) }
    var seconds by remember { mutableIntStateOf(0) }
    var isVibrate by remember { mutableStateOf(true) }
    var showSoundPicker by remember { mutableStateOf(false) }
    var selectedSound by remember { mutableStateOf<AlarmSound?>(null) }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedSound = AlarmSound(it, "Tùy chỉnh")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm hẹn giờ mới") },
        text = {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NumberPicker(
                        value = hours,
                        onValueChange = { hours = it },
                        range = 0..23,
                        label = "Giờ"
                    )
                    NumberPicker(
                        value = minutes,
                        onValueChange = { minutes = it },
                        range = 0..59,
                        label = "Phút"
                    )
                    NumberPicker(
                        value = seconds,
                        onValueChange = { seconds = it },
                        range = 0..59,
                        label = "Giây"
                    )
                }

                // Sound Selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSoundPicker = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Âm thanh :",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedSound?.title ?: "Mặc định",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_right),
                        contentDescription = "Chọn âm thanh"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rung :")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isVibrate,
                        onCheckedChange = { isVibrate = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        hours,
                        minutes,
                        seconds,
                        selectedSound?.uri?.toString() ?: "",
                        isVibrate
                    )
                },
                enabled = hours > 0 || minutes > 0 || seconds > 0
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )

    if (showSoundPicker) {
        AlertDialog(
            modifier = Modifier.height(500.dp),
            onDismissRequest = { showSoundPicker = false },
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Âm thanh hẹn giờ",
                    style = MaterialTheme.typography.titleLarge,
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
                            Text("Chọn từ bộ nhớ")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(thickness = 2.dp)
                    }

                    items(alarmSounds) { sound ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSound = sound
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
                                        if (sound.uri.toString().isBlank())
                                            R.drawable.ic_noti_off
                                        else
                                            R.drawable.ic_noti_on
                                    ),
                                    contentDescription = "Sound icon"
                                )
                                Text(sound.title)
                            }

                            if (selectedSound?.uri == sound.uri) {
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

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = {
                if (value < range.last) onValueChange(value + 1)
            }
        ) {
            Text("▲")
        }

        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.headlineMedium
        )

        IconButton(
            onClick = {
                if (value > range.first) onValueChange(value - 1)
            }
        ) {
            Text("▼")
        }

        Text(label)
    }
}

@SuppressLint("DefaultLocale")
private fun formatTime(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@Preview
@Composable
private fun TimerPreview() {
    TimerScreen(
        timers = emptyList(),
        alarmSounds = emptyList(),
        onAddTimer = { _, _, _, _, _ -> },
        onPauseTimer = { },
        onResumeTimer = { },
        onStopTimer = { },
        onAddOneMinute = { },
        onResetTimer = { }
    )
}
