package com.anhq.smartalarm.features.editalarm

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhq.smartalarm.R
import com.anhq.smartalarm.core.designsystem.component.GameTypeSelector
import com.anhq.smartalarm.core.designsystem.theme.Pure01
import com.anhq.smartalarm.core.designsystem.theme.Pure02
import com.anhq.smartalarm.core.designsystem.theme.body2
import com.anhq.smartalarm.core.designsystem.theme.gradient1
import com.anhq.smartalarm.core.designsystem.theme.label1
import com.anhq.smartalarm.core.designsystem.theme.title3
import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.core.model.DayOfWeek
import com.anhq.smartalarm.core.ui.InputTextDialog
import com.anhq.smartalarm.core.utils.AlarmSound
import com.commandiron.wheel_picker_compose.WheelTimePicker
import com.commandiron.wheel_picker_compose.core.TimeFormat
import com.commandiron.wheel_picker_compose.core.WheelPickerDefaults
import java.time.LocalTime

@Composable
fun EditAlarmRoute(
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    val viewModel: EditAlarmViewModel = hiltViewModel()
    val permissionRequired = viewModel.permissionRequired.value

    val label by viewModel.label.collectAsStateWithLifecycle()
    val selectedDays by viewModel.selectedDays.collectAsStateWithLifecycle()
    val isVibrate by viewModel.isVibrate.collectAsStateWithLifecycle()
    val gameType by viewModel.gameType.collectAsStateWithLifecycle()
    val alarmSounds by viewModel.alarmSounds.collectAsStateWithLifecycle()
    val selectedSound by viewModel.selectedSound.collectAsStateWithLifecycle()
    val selectedTime by viewModel.selectedTime.collectAsStateWithLifecycle()

    EditAlarmScreen(
        onCancelClick = onCancelClick,
        onSaveClick = {
            if (permissionRequired == true) {
                viewModel.getExactAlarmPermissionIntent()
            } else {
                viewModel.updateAlarm()
                onSaveClick()
            }
        },
        label = label,
        setLabel = { viewModel.setLabel(it) },
        selectedTime = selectedTime,
        setSelectedTime = { viewModel.setSelectedTime(it) },
        selectedDays = selectedDays,
        toggleDay = { viewModel.toggleDay(it) },
        isVibrate = isVibrate,
        setVibrate = { viewModel.setIsVibrate(!isVibrate) },
        gameType = gameType,
        setGameType = { viewModel.setGameType(it) },
        alarmSounds = alarmSounds,
        selectedSound = selectedSound,
        setAlarmSound = { viewModel.setAlarmSound(it) },
        previewAlarm = { viewModel.previewAlarm() },
        stopPreview = { viewModel.stopPreview() }
    )
}
@Composable
fun EditAlarmScreen(
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    label: String,
    setLabel: (String) -> Unit,
    selectedDays: Set<DayOfWeek>,
    toggleDay: (DayOfWeek) -> Unit,
    selectedTime: LocalTime,
    setSelectedTime: (LocalTime) -> Unit,
    isVibrate: Boolean,
    setVibrate: () -> Unit,
    gameType: AlarmGameType,
    setGameType: (AlarmGameType) -> Unit,
    alarmSounds: List<AlarmSound>,
    selectedSound: AlarmSound?,
    setAlarmSound: (Uri) -> Unit,
    previewAlarm: () -> Intent?,
    stopPreview: () -> Unit
) {
    var isEditName by remember { mutableStateOf(false) }
    var showSoundPicker by remember { mutableStateOf(false) }
    var isPreviewing by remember { mutableStateOf(false) }
    
    val gameLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK || result.resultCode == Activity.RESULT_CANCELED) {
            isPreviewing = false
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            setAlarmSound(it)
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            stopPreview()
        }
    }

    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.clickable { onCancelClick() },
                        text = stringResource(R.string.cancel),
                        style = gradient1,
                    )
                    Text(
                        text = stringResource(R.string.edit),
                        style = title3,
                    )
                    Text(
                        modifier = Modifier.clickable { onSaveClick() },
                        text = stringResource(R.string.save),
                        style = gradient1,
                    )
                }

                // Time Input Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Alarm Name
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = title3,
                            )
                            IconButton(onClick = { isEditName = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_pencil),
                                    contentDescription = "Edit alarm name"
                                )
                            }
                        }

                        // Time Picker
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            WheelTimePicker(
                                startTime = selectedTime,
                                timeFormat = TimeFormat.HOUR_24,
                                size = DpSize(300.dp, 150.dp),
                                rowCount = 3,
                                textStyle = MaterialTheme.typography.headlineMedium,
                                textColor = MaterialTheme.colorScheme.onBackground,
                                selectorProperties = WheelPickerDefaults.selectorProperties(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ),
                                onSnappedTime = { time ->
                                    setSelectedTime(time)
                                }
                            )
                        }
                    }
                }

                // Settings Section
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Repeat Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.repeat_days),
                                    style = title3,
                                    modifier = Modifier.padding(bottom = 20.dp)
                                )
                                RepeatAlarmButton(
                                    selectedDays = selectedDays,
                                    toggleDay = toggleDay
                                )
                            }
                        }
                    }

                    // Game Type Section
                    item {
                        GameTypeSelector(
                            selectedGameType = gameType,
                            onGameTypeSelected = setGameType
                        )
                    }

                    // Sound Settings Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                // Sound Selection
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showSoundPicker = true },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.sound),
                                            style = title3
                                        )
                                        Text(
                                            text = selectedSound?.title ?: "Mặc định",
                                            style = body2,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        painter = painterResource(R.drawable.ic_arrow_right),
                                        contentDescription = "Select sound"
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Vibration Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Rung",
                                        style = title3
                                    )
                                    Switch(
                                        checked = isVibrate,
                                        onCheckedChange = { setVibrate() },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFFB388FF)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Preview/Stop Button
                    item {
                        Button(
                            onClick = {
                                if (isPreviewing) {
                                    stopPreview()
                                    isPreviewing = false
                                } else {
                                    val gameIntent = previewAlarm()
                                    gameIntent?.let { intent ->
                                        gameLauncher.launch(intent)
                                        isPreviewing = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_game),
                                    contentDescription = "Preview alarm",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Xem trước báo thức",
                                    style = title3
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // Dialogs
                if (isEditName) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        InputTextDialog(
                            name = label,
                            onDismiss = { isEditName = false },
                            onConfirm = {
                                setLabel(it)
                                isEditName = false
                            }
                        )
                    }
                }

                if (showSoundPicker) {
                    AlertDialog(
                        modifier = Modifier.height(500.dp),
                        onDismissRequest = { showSoundPicker = false }, title = {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = "Âm thanh báo thức",
                                style = title3,
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
                                            style = body2
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
                                                setAlarmSound(sound.uri)
                                                showSoundPicker = false
                                            }
                                            .padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically

                                        ) {
                                            Icon(
                                                painter = painterResource(
                                                    if (sound.uri.toString()
                                                            .isBlank()
                                                    ) R.drawable.ic_noti_off else R.drawable.ic_noti_on
                                                ), contentDescription = "Selected"
                                            )
                                            Text(
                                                sound.title, style = body2
                                            )
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
                        })
                }
            }
        }
    )
}

@Composable
fun RepeatAlarmButton(
    selectedDays: Set<DayOfWeek>,
    toggleDay: (DayOfWeek) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DayOfWeek.entries.forEach { day ->
            val isSelected = selectedDays.contains(day)
            Button(
                onClick = { toggleDay(day) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .width(42.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) {
                            Brush.horizontalGradient(colors = listOf(Pure01, Pure02))
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF2C2C2E),
                                    Color(0xFF2C2C2E)
                                )
                            )
                        }
                    )
            ) {
                Text(
                    text = day.label,
                    style = label1,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditAlarmScreenPreview() {
    EditAlarmScreen(
            onCancelClick = {},
            onSaveClick = {},
            label = "Morning Alarm",
            setLabel = {},
            selectedDays = setOf(DayOfWeek.MON, DayOfWeek.WED),
            toggleDay = {},
        selectedTime = LocalTime.of(7, 30),
        setSelectedTime = {},
            isVibrate = true,
            setVibrate = {},
            gameType = AlarmGameType.MATH_PROBLEM,
            setGameType = {},
            alarmSounds = listOf(
                AlarmSound(title = "Classic Ring", uri = "android.resource://com.anhq.smartalarm/raw/ring1".toUri()),
                AlarmSound(title = "Gentle Wake", uri = "android.resource://com.anhq.smartalarm/raw/ring2".toUri())
            ),
            selectedSound = AlarmSound(title = "Classic Ring", uri = "android.resource://com.anhq.smartalarm/raw/ring1".toUri()),
            setAlarmSound = {},
            previewAlarm = { null },
        stopPreview = {}
        )
}