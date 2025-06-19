package com.anhq.smartalarm.features.addalarm

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhq.smartalarm.R
import com.anhq.smartalarm.core.database.model.AlarmSuggestionEntity
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
import java.util.Calendar

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
    val selectedTime by viewModel.selectedTime.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()

    // Load suggestions based on selected days
    LaunchedEffect(selectedDays) {
        if (selectedDays.isEmpty()) {
            // If no day selected, show suggestions for current day
            val calendar = Calendar.getInstance()
            val calendarDay = calendar.get(Calendar.DAY_OF_WEEK)
            // Convert from Calendar.DAY_OF_WEEK to our DayOfWeek enum
            val currentDay = when (calendarDay) {
                Calendar.MONDAY -> DayOfWeek.MON
                Calendar.TUESDAY -> DayOfWeek.TUE
                Calendar.WEDNESDAY -> DayOfWeek.WED
                Calendar.THURSDAY -> DayOfWeek.THU
                Calendar.FRIDAY -> DayOfWeek.FRI
                Calendar.SATURDAY -> DayOfWeek.SAT
                Calendar.SUNDAY -> DayOfWeek.SUN
                else -> DayOfWeek.MON // Fallback to Monday
            }
            viewModel.loadSuggestionsForDay(currentDay)
        } else {
            // Load suggestions for all selected days
            selectedDays.forEach { day ->
                viewModel.loadSuggestionsForDay(day)
            }
        }
    }

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
        stopPreview = { viewModel.stopPreview() },
        suggestions = suggestions,
        onSuggestionSelected = { suggestion, accepted ->
            // Đảm bảo cập nhật thời gian trước
            val newTime = LocalTime.of(suggestion.hour, suggestion.minute)
            viewModel.setSelectedTime(newTime)
            // Sau đó mới đánh dấu suggestion đã được chấp nhận
            viewModel.onSuggestionSelected(suggestion, accepted)
        }
    )
}

@Composable
fun AddAlarmScreen(
    onCancelClick: () -> Unit,
    onAddClick: () -> Unit,
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
    stopPreview: () -> Unit,
    suggestions: List<AlarmSuggestionEntity>,
    onSuggestionSelected: (AlarmSuggestionEntity, Boolean) -> Unit
) {
    var isEditName by remember { mutableStateOf(false) }
    var showSoundPicker by remember { mutableStateOf(false) }
    var isPreviewing by remember { mutableStateOf(false) }

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
                        style = gradient1,
                    )
                    Text(
                        text = stringResource(R.string.add),
                        style = title3,
                    )
                    Text(
                        modifier = Modifier.clickable { onAddClick() },
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
                            key (selectedTime) {
                                WheelTimePicker(
                                    startTime = selectedTime,
                                    timeFormat = TimeFormat.HOUR_24,
                                    size = DpSize(300.dp, 150.dp),
                                    rowCount = 3,
                                    textStyle = MaterialTheme.typography.headlineMedium,
                                    textColor = MaterialTheme.colorScheme.onBackground,
                                    selectorProperties = WheelPickerDefaults.selectorProperties(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(
                                            alpha = 0.3f
                                        )
                                    ),
                                    onSnappedTime = { time ->
                                        setSelectedTime(time)
                                    }
                                )
                            }
                        }
                    }
                }
                AnimatedVisibility(suggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.suggested_times),
                                style = title3,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                suggestions.forEach { suggestion ->
                                    SuggestionChip(
                                        onClick = {
                                            // Đảm bảo cập nhật thời gian trước
                                            val newTime =
                                                LocalTime.of(suggestion.hour, suggestion.minute)
                                            setSelectedTime(newTime)
                                            // Sau đó mới đánh dấu suggestion đã được chấp nhận
                                            onSuggestionSelected(suggestion, true)
                                        },
                                        label = {
                                            Text(
                                                text = stringResource(
                                                    R.string.alarm_suggestion_format,
                                                    suggestion.hour,
                                                    suggestion.minute,
                                                    suggestion.dayOfWeek.toString()
                                                ),
                                                style = body2
                                            )
                                        }
                                    )
                                }
                            }
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
                                    style = title3,
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
                                            text = stringResource(R.string.sound), style = title3
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
                                        text = "Rung", style = title3
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
                    style = label1,
                    color = Color.White
                )
            }
        }
    }
}

@Preview
@Composable
fun AddAlarmScreenPreview() {
    val suggestions = listOf(
        AlarmSuggestionEntity(
            id = 1,
            hour = 7,
            minute = 30,
            dayOfWeek = DayOfWeek.MON,
            confidence = 0.85f,
            lastUpdated = System.currentTimeMillis(),
            suggestedCount = 5,
            acceptedCount = 4
        ),
        AlarmSuggestionEntity(
            id = 2,
            hour = 8,
            minute = 0,
            dayOfWeek = DayOfWeek.MON,
            confidence = 0.75f,
            lastUpdated = System.currentTimeMillis(),
            suggestedCount = 3,
            acceptedCount = 2
        )
    )
    AddAlarmScreen(
        onCancelClick = { },
        onAddClick = { },
        label = "Báo thức buổi sáng",
        selectedTime = LocalTime.of(7, 30),
        setSelectedTime = { },
        setLabel = { },
        toggleDay = { },
        setVibrate = { },
        setGameType = { },
            selectedDays = setOf(DayOfWeek.MON, DayOfWeek.WED, DayOfWeek.FRI),
            gameType = AlarmGameType.MATH_PROBLEM,
        isVibrate = true,
            alarmSounds = listOf(
                AlarmSound(uri = "".toUri(), title = "Im lặng"),
                AlarmSound(uri = "content://media/alarm1".toUri(), title = "Báo thức 1"),
                AlarmSound(uri = "content://media/alarm2".toUri(), title = "Báo thức 2")
            ),
        selectedSound = AlarmSound(
            uri = "content://media/alarm1".toUri(),
            title = "Báo thức 1"
        ),
        setAlarmSound = { },
        previewAlarm = { null },
        stopPreview = { },
        suggestions = suggestions,
        onSuggestionSelected = { _, _ -> }
        )
}