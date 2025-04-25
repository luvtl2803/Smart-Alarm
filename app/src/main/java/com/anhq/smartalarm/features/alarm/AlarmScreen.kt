package com.anhq.smartalarm.features.alarm

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.anhq.smartalarm.core.designsystem.theme.label1
import com.anhq.smartalarm.core.model.Alarm
import com.anhq.smartalarm.features.addalarm.navigation.navigateToAddAlarm
import com.anhq.smartalarm.features.editalarm.navigation.navigateToEditAlarm
import kotlin.math.roundToInt

@Composable
fun AlarmRoute(
    navController: NavController,
) {
    val viewModel: AlarmViewModel = hiltViewModel()
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()

    AlarmScreen(
        alarms = alarms,
        setAlarmActive = { alarm, isActive ->
            viewModel.updateAlarmStatus(alarm = alarm, isActive = isActive)
        },
        onEditClick = { alarmId ->
            navController.navigateToEditAlarm(id = alarmId)
        },
        onAddClick = {
            navController.navigateToAddAlarm()
        }
    )
}

@Composable
fun AlarmScreen(
    alarms: List<Alarm>,
    setAlarmActive: (Alarm, Boolean) -> Unit,
    onEditClick: (Int) -> Unit,
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
                }
                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        alarms.forEach { alarm ->
                            AlarmCard(
                                alarm = alarm,
                                onEditClick = onEditClick,
                                setAlarmActive = { isActive ->
                                    setAlarmActive(alarm, isActive)
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }

            var offsetX by remember { mutableFloatStateOf((-30.946579).toFloat()) }
            var offsetY by remember { mutableFloatStateOf((-109.210396).toFloat()) }

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
        }
    )
}

@Composable
fun SwipeToDismissListItems() {
    val dismissState = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by
            animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> Color.LightGray
                    SwipeToDismissBoxValue.StartToEnd -> Color.Green
                    SwipeToDismissBoxValue.EndToStart -> Color.Red
                }
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
            )
        }
    ) {
        OutlinedCard(shape = RectangleShape) {
            ListItem(
                headlineContent = { Text("Cupcake") },
                supportingContent = { Text("Swipe me left or right!") }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlarmCard(alarm: Alarm, onEditClick: (Int) -> Unit, setAlarmActive: (Boolean) -> Unit) {
    val gradient = if (alarm.isActive) {
        Brush.linearGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF9C27B0), Color(0xFF673AB7)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    onEditClick(alarm.id)
                },
                onLongClick = {
                    setAlarmActive(!alarm.isActive)
                }
            )
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
                checked = alarm.isActive,
                onCheckedChange = {
                    setAlarmActive(!alarm.isActive)
                }
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
                isActive = true,
                isVibrate = true,
                timeInMillis = 1
            )
        ),
        setAlarmActive = { _, _ -> },
        onEditClick = { },
        onAddClick = { }
    )
}