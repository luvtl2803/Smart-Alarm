package com.anhq.smartalarm.features.editalarm

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anhq.smartalarm.R
import com.anhq.smartalarm.core.designsystem.theme.Pure01
import com.anhq.smartalarm.core.designsystem.theme.Pure02
import com.anhq.smartalarm.core.designsystem.theme.body4
import com.anhq.smartalarm.core.designsystem.theme.body5
import com.anhq.smartalarm.core.designsystem.theme.gradient1
import com.anhq.smartalarm.core.designsystem.theme.label1
import com.anhq.smartalarm.core.designsystem.theme.label2
import com.anhq.smartalarm.core.designsystem.theme.label3
import com.anhq.smartalarm.core.model.AlarmCount
import com.anhq.smartalarm.core.model.AlarmGame
import com.anhq.smartalarm.core.model.AlarmSetType
import com.anhq.smartalarm.core.model.NameOfGame
import com.anhq.smartalarm.core.ui.InputTextDialog
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAlarmRoute() {

    val viewModel: EditAlarmViewModel = hiltViewModel()
    val permissionRequired = viewModel.permissionRequired.value

    val label by viewModel.label.collectAsStateWithLifecycle()
    val timeInMills by viewModel.timeInMills.collectAsStateWithLifecycle()
    val repeatDays by viewModel.repeatDays.collectAsStateWithLifecycle()
    val isVibrate by viewModel.isVibrate.collectAsStateWithLifecycle()

    EditAlarmScreen(
        type = viewModel.type,
        saveAlarmClick = {
            viewModel.setAlarm()
        },
        label = label,
        setLabel = {
            viewModel.setLabel(it)
        },
        timeInMills = timeInMills,
        setTimeInMills = {
            if (permissionRequired == true) {
                viewModel.getExactAlarmPermissionIntent()
            } else {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, it.hour)
                    set(Calendar.MINUTE, it.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                viewModel.setTimeInMills(calendar.timeInMillis)
            }
        },
        repeatDays = repeatDays,
        setRepeatDays = {
            viewModel.setRepeatDays(it)
        },
        isVibrate = isVibrate,
        setVibrate = {
            viewModel.setIsVibrate(!isVibrate)
        },

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAlarmScreen(
    type: AlarmSetType,
    saveAlarmClick: () -> Unit,
    label: String,
    setLabel: (String) -> Unit,
    repeatDays: List<Int>,
    setRepeatDays: (List<Int>) -> Unit,
    timeInMills: Long,
    setTimeInMills: (TimePickerState) -> Unit,
    isVibrate: Boolean,
    setVibrate: () -> Unit,
) {
    var isEditName by remember { mutableStateOf(false) }

    val currentTime =
        if (type == AlarmSetType.CREATE) Calendar.getInstance() else Calendar.getInstance().apply {
            timeInMillis = timeInMills
        }

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = false,
    )


    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(WindowInsets.statusBars.asPaddingValues()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.clickable {
                            setTimeInMills(timePickerState)
                            saveAlarmClick()
                        },
                        text = "Cancel",
                        style = MaterialTheme.typography.gradient1,
                    )
                    Text(
                        text = "Set Alarm",
                        style = MaterialTheme.typography.label1,
                    )

                    Text(
                        modifier = Modifier.clickable {
                            setTimeInMills(timePickerState)
                            saveAlarmClick()
                        },
                        text = "Save",
                        style = MaterialTheme.typography.gradient1,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.label2,
                    )
                    IconButton(
                        onClick = { isEditName = !isEditName }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_pencil),
                            contentDescription = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TimeInput(
                    state = timePickerState
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Repeat",
                                style = MaterialTheme.typography.label2
                            )

                            IconButton(
                                onClick = { }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_down),
                                    contentDescription = null
                                )
                            }
                        }
                    }


                    item {
                        RepeatAlarmButton(
                            alarmCounts = AlarmCount.defaultCounts.toList(),
                            repeatDays = repeatDays,
                            setRepeatDays = { setRepeatDays(it) })
                    }

                    item {
                        SoundAlarmButton()
                    }

                    item {
                        VolumeSlider()
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Vibrate",
                                style = MaterialTheme.typography.label2
                            )

                            Switch(
                                checked = isVibrate,
                                onCheckedChange = { setVibrate() } ,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFB388FF)
                                )
                            )
                        }
                    }

                    item {
                        Row(
                            Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Alarm Game",
                                style = MaterialTheme.typography.label2
                            )
                        }

                        Spacer(Modifier.height(20.dp))

//                        GameAlarmButton(
//                            alarmGames = AlarmGame.defaultGames,
//                            selectedGame = selectedGame.value,
//                            onGameSelected = { selectedGame.value = it }
//                        )
                    }

                    item {
                        PreviewAlarm()
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }

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
            }
        }
    )
}

@Composable
fun RepeatAlarmButton(
    alarmCounts: List<AlarmCount>,
    repeatDays: List<Int>,
    setRepeatDays: (List<Int>) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        alarmCounts.forEachIndexed { index, alarmCount ->
            val isSelected = repeatDays.contains(index)

            Button(
                onClick = {
                    val updatedDays = repeatDays.toMutableList()
                    if (isSelected) {
                        updatedDays.remove(index)
                    } else {
                        updatedDays.add(index)
                    }
                    setRepeatDays(updatedDays)
                },
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
                            Brush.horizontalGradient(
                                colors = listOf(Pure01, Pure02)
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF2C2C2E), Color(0xFF2C2C2E))
                            )
                        }
                    )
            ) {
                Text(
                    text = alarmCount.dayOfWeek.label,
                    style = MaterialTheme.typography.label3,
                    color = Color.White
                )
            }
        }
    }
}


@Composable
fun SoundAlarmButton(

) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sound",
                style = MaterialTheme.typography.label2
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "My sound 123", style = MaterialTheme.typography.body5
            )
        }

        IconButton(
            onClick = { }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null
            )
        }
    }
}

@Composable
fun VolumeSlider(

) {
    var sliderPosition by remember { mutableFloatStateOf(0.4f) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Volume",
                style = MaterialTheme.typography.label2
            )

            Text(
                text = "${(sliderPosition * 100).toInt()}%",
                style = MaterialTheme.typography.label2
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Slider(
        modifier = Modifier
            .height(10.dp)
            .padding(horizontal = 10.dp),
        value = sliderPosition,
        onValueChange = {
            sliderPosition = it
        },
        valueRange = 0f..1f,
    )
}

@Composable
fun GameAlarmButton(
    alarmGames: List<AlarmGame>,
    selectedGame: NameOfGame?,
    onGameSelected: (NameOfGame) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(alarmGames) { alarmGame ->
            val isSelected = alarmGame.gameName == selectedGame

            Card(
                modifier = Modifier
                    .width(100.dp)
                    .height(120.dp)
                    .border(
                        width = if (isSelected) 5.dp else 0.dp,
                        color = if (isSelected) Color.Cyan else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onGameSelected(alarmGame.gameName) },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF3C3D3F)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "Game Icon",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = alarmGame.gameName.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
fun PreviewAlarm() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Preview Alarm",
            style = MaterialTheme.typography.label2
        )

        IconButton(
            onClick = { }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun EditAlarmScreenPreview() {
    EditAlarmScreen(
        type = AlarmSetType.CREATE,
        timeInMills = 0L,
        saveAlarmClick = {},
        setTimeInMills = {
            Log.d("AlarmRoute", "AlarmRoute: $it")
        },
        label = "Alarm Name",
        setLabel = {},
        repeatDays = listOf(),
        setRepeatDays = {},
        isVibrate = false,
        setVibrate = {}
    )
}
