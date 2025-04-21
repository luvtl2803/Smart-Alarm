package com.anhq.smartalarm.features.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.anhq.smartalarm.core.designsystem.theme.label1
import com.anhq.smartalarm.core.model.Alarm
import com.anhq.smartalarm.core.model.AlarmSetType
import com.anhq.smartalarm.features.editalarm.navigation.navigateToEditAlarm
import kotlin.math.roundToInt

@Composable
fun AlarmRoute(
    navController: NavController,
    viewModel: AlarmViewModel = viewModel()
) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()

    AlarmScreen(
        alarms = alarms,
        onToggle = { index, isEnabled -> viewModel.toggleAlarm(index, isEnabled) },
        onEditClick = { /* Xử lý chỉnh sửa sau */ },
        onAddClick = {
            navController.navigateToEditAlarm(id = 0, type = AlarmSetType.CREATE)
        }
    )
}

@Composable
fun AlarmScreen(
    alarms: List<Alarm>,
    onToggle: (Int, Boolean) -> Unit,
    onEditClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(WindowInsets.statusBars.asPaddingValues()),
                verticalArrangement = Arrangement.Center,
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
                        text = "Alarm",
                        style = MaterialTheme.typography.label1,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Edit",
                        color = Color(0xFFB388FF),
                        modifier = Modifier.clickable { onEditClick() }
                    )
                }
                Spacer(modifier = Modifier.height(50.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        alarms.forEachIndexed { index, alarm ->
                            AlarmCard(alarm = alarm, onToggle = { onToggle(index, it) })
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            var offsetX by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }

            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        },
                    onClick = onAddClick,
                    shape = CircleShape,
                    content = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White
                        )
                    },
                    containerColor = Color(0xFFB388FF),
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    )
}

@Composable
fun AlarmCard(alarm: Alarm, onToggle: (Boolean) -> Unit) {
    val gradient = if (alarm.isEnabled) {
        Brush.linearGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF9C27B0), Color(0xFF673AB7)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = gradient, shape = RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = alarm.label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${alarm.hour}:${alarm.minute.toString().padStart(2, '0')}",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alarm.getRepeatDaysString(),
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    text = alarm.getCountdownTime(System.currentTimeMillis()),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Preview
@Composable
private fun AlarmPreview() {
    AlarmScreen(
        alarms = listOf(
            Alarm(
                id = 0,
                hour = 5,
                minute = 30,
                repeatDays = listOf(1, 2, 4, 5),
                label = "Morning Alarm",
                isEnabled = true,
                isVibrate = true,
                timeInMillis = System.currentTimeMillis() + 12 * 60 * 60 * 1000 + 28 * 60 * 1000
            )
        ),
        onToggle = { _, _ -> },
        onEditClick = { },
        onAddClick = { }
    )
}