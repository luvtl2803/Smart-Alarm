package com.anhq.smartalarm.features.addalarm

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhq.smartalarm.R
import com.anhq.smartalarm.core.designsystem.component.GameTypeSelector
import com.anhq.smartalarm.core.designsystem.theme.Pure01
import com.anhq.smartalarm.core.designsystem.theme.Pure02
import com.anhq.smartalarm.core.designsystem.theme.SmartAlarmTheme
import com.anhq.smartalarm.core.designsystem.theme.body4
import com.anhq.smartalarm.core.designsystem.theme.body5
import com.anhq.smartalarm.core.designsystem.theme.gradient1
import com.anhq.smartalarm.core.designsystem.theme.label1
import com.anhq.smartalarm.core.designsystem.theme.label2
import com.anhq.smartalarm.core.designsystem.theme.label3
import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.core.model.DayOfWeek
import com.anhq.smartalarm.core.ui.InputTextDialog
import com.anhq.smartalarm.core.utils.AlarmSound
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmRoute(
    onCancelClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    val viewModel: AddAlarmViewModel = hiltViewModel()
    val permissionRequired = viewModel.permissionRequired.value
    val context = LocalContext.current

    val label by viewModel.label.collectAsStateWithLifecycle()
    val selectedDays by viewModel.selectedDays.collectAsStateWithLifecycle()
    val isVibrate by viewModel.isVibrate.collectAsStateWithLifecycle()
    val gameType by viewModel.gameType.collectAsStateWithLifecycle()
    val alarmSounds by viewModel.alarmSounds.collectAsStateWithLifecycle()
    val selectedSound by viewModel.selectedSound.collectAsStateWithLifecycle()

    AddAlarmScreen(
        onCancelClick = onCancelClick,
        onAddClick = {
            if (permissionRequired == true) {
                viewModel.getExactAlarmPermissionIntent()?.let { intent ->
                    context.startActivity(intent)
                }
            } else {
                viewModel.saveAlarm()
                onAddClick()
            }
        },
        label = label,
        setLabel = { viewModel.setLabel(it) },
        setTimePickerState = { viewModel.getTimePickerState(it) },
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
        stopPreview = { viewModel.stopPreview() })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmScreen(
    onCancelClick: () -> Unit,
    onAddClick: () -> Unit,
    label: String,
    setLabel: (String) -> Unit,
    selectedDays: Set<DayOfWeek>,
    toggleDay: (DayOfWeek) -> Unit,
    setTimePickerState: (TimePickerState) -> Unit,
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
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

        // Activity result launcher for game preview
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
                        style = MaterialTheme.typography.gradient1,
                    )
                    Text(
                        text = stringResource(R.string.add),
                        style = MaterialTheme.typography.label1,
                    )
                    Text(
                        modifier = Modifier.clickable {
                            setTimePickerState(timePickerState)
                            onAddClick()
                        },
                        text = stringResource(R.string.save),
                        style = MaterialTheme.typography.gradient1,
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
                                style = MaterialTheme.typography.label2,
                            )
                            IconButton(onClick = { isEditName = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_pencil),
                                    contentDescription = "Edit alarm name"
                                )
                            }
                        }

                        // Time Picker
                        TimeInput(
                            state = timePickerState, modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // Settings Section
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Repeat Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
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
                                    style = MaterialTheme.typography.label2,
                                    modifier = Modifier.padding(bottom = 20.dp)
                                )
                                RepeatAlarmButton(
                                    selectedDays = selectedDays, toggleDay = toggleDay
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
                            modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
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
                                            text = stringResource(R.string.sound), style = MaterialTheme.typography.label2
                                        )
                                        Text(
                                            text = selectedSound?.title ?: "Mặc định",
                                            style = MaterialTheme.typography.body4,
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
                                        text = "Rung", style = MaterialTheme.typography.label2
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
                                    style = MaterialTheme.typography.label2
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
                            style = MaterialTheme.typography.label1,
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
                                            style = MaterialTheme.typography.body4
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
                                                sound.title, style = MaterialTheme.typography.body5
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
    selectedDays: Set<DayOfWeek>, toggleDay: (DayOfWeek) -> Unit
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
                                    Color(0xFF2C2C2E), Color(0xFF2C2C2E)
                                )
                            )
                        }
                    )
            ) {
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.label3,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun AddAlarmScreenPreview() {

    SmartAlarmTheme {
        AddAlarmScreen(
            onCancelClick = { /* No-op for preview */ },
            onAddClick = { /* No-op for preview */ },
            label = "Báo thức ",
            setLabel = { /* No-op for preview */ },
            setTimePickerState = { /* No-op for preview */ },
            selectedDays = setOf(DayOfWeek.MON, DayOfWeek.WED, DayOfWeek.FRI),
            toggleDay = { /* No-op for preview */ },
            isVibrate = true,
            setVibrate = { /* No-op for preview */ },
            gameType = AlarmGameType.MATH_PROBLEM,
            setGameType = { /* No-op for preview */ },
            alarmSounds = listOf(
                AlarmSound(uri = "content://media/alarm1".toUri(), title = "Alarm 1"),
                AlarmSound(uri = "content://media/alarm2".toUri(), title = "Alarm 2"),
                AlarmSound(uri = "content://media/alarm3".toUri(), title = "Alarm 3")
            ),
            selectedSound = AlarmSound(uri = "content://media/alarm1".toUri(), title = "Alarm 1"),
            setAlarmSound = { /* No-op for preview */ },
            previewAlarm = { null }, // Trả về null vì không cần Intent trong preview
            stopPreview = { /* No-op for preview */ }
        )
    }
}