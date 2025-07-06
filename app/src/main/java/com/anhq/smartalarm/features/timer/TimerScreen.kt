package com.anhq.smartalarm.features.timer

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anhq.smartalarm.R
import com.anhq.smartalarm.core.designsystem.theme.headline3
import com.anhq.smartalarm.core.designsystem.theme.title2
import com.anhq.smartalarm.core.model.Timer
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.duration.DurationDialog
import com.maxkeppeler.sheets.duration.models.DurationConfig
import com.maxkeppeler.sheets.duration.models.DurationFormat
import com.maxkeppeler.sheets.duration.models.DurationSelection
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun TimerRoute(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val timers by viewModel.timers.collectAsState()

    TimerScreen(
        timers = timers,
        onAddTimer = viewModel::addTimer,
        onPauseTimer = viewModel::pauseTimer,
        onResumeTimer = viewModel::resumeTimer,
        onStopTimer = viewModel::stopTimer,
        onAddOneMinute = viewModel::addOneMinute,
        onResetTimer = viewModel::resetTimer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    timers: List<Timer>,
    onAddTimer: (Long) -> Unit,
    onPauseTimer: (Int) -> Unit,
    onResumeTimer: (Int) -> Unit,
    onStopTimer: (Int) -> Unit,
    onAddOneMinute: (Int) -> Unit,
    onResetTimer: (Int) -> Unit
) {
    val dialogState = remember {
        mutableStateOf(
            DialogState(
                isVisible = false,
                timeInSeconds = 0L
            )
        )
    }

    Scaffold(
        modifier = Modifier.padding(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        ),
        topBar = {
            Text(
                text = "Hẹn giờ",
                style = headline3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textAlign = TextAlign.Center
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    dialogState.value = DialogState(isVisible = true, timeInSeconds = 0L)
                },
                shape = RoundedCornerShape(50.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm hẹn giờ")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            TimerList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                timers = timers,
                onPauseTimer = onPauseTimer,
                onResumeTimer = onResumeTimer,
                onStopTimer = onStopTimer,
                onAddOneMinute = onAddOneMinute,
                onResetTimer = onResetTimer
            )

            if (dialogState.value.isVisible) {
                TimerDurationDialog(
                    dialogState = dialogState,
                    onAddTimer = onAddTimer
                )
            }
        }
    }
}

@Composable
private fun TimerList(
    modifier: Modifier = Modifier,
    timers: List<Timer>,
    onPauseTimer: (Int) -> Unit,
    onResumeTimer: (Int) -> Unit,
    onStopTimer: (Int) -> Unit,
    onAddOneMinute: (Int) -> Unit,
    onResetTimer: (Int) -> Unit
) {
    Column(
        modifier = modifier,
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
                    style = title2,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = timers,
                    key = { it.id }
                ) { timer ->
                    key(timer.id) {
                        val displayTime by remember(
                            timer.remainingTimeMillis,
                            timer.lastTickTime
                        ) {
                            derivedStateOf {
                                if (timer.isPaused) {
                                    timer.remainingTimeMillis
                                } else {
                                    val now = System.currentTimeMillis()
                                    val elapsed = now - timer.lastTickTime
                                    (timer.remainingTimeMillis - elapsed).coerceAtLeast(0)
                                }
                            }
                        }

                        TimerCard(
                            timer = timer,
                            displayTime = displayTime,
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimerDurationDialog(
    dialogState: MutableState<DialogState>,
    onAddTimer: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    val durationState = rememberUseCaseState()

    // Reset dialog state when it's dismissed
    LaunchedEffect(dialogState.value.isVisible) {
        if (dialogState.value.isVisible) {
            durationState.show()
        } else {
            durationState.hide()
        }
    }

    DurationDialog(
        state = durationState,
        selection = DurationSelection(
            onNegativeClick = {
                durationState.hide()
                dialogState.value = DialogState(isVisible = false, timeInSeconds = 0L)
            },
            onPositiveClick = { timeInSeconds ->
                scope.launch {
                    val totalMillis = timeInSeconds * 1000L
                    onAddTimer(totalMillis)
                    durationState.hide()
                    dialogState.value =
                        DialogState(isVisible = false, timeInSeconds = timeInSeconds)
                }
            }
        ),
        config = DurationConfig(
            timeFormat = DurationFormat.HH_MM_SS,
            currentTime = dialogState.value.timeInSeconds,
            minTime = 1,
            maxTime = 24 * 60 * 60,
            displayClearButton = false
        )
    )
}

private data class DialogState(
    val isVisible: Boolean = false,
    val timeInSeconds: Long = 0L
)

@Composable
fun TimerCard(
    timer: Timer,
    displayTime: Long,
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
                    text = formatTime(displayTime),
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

private fun formatTime(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}

@Preview
@Composable
private fun TimerPreview() {
    TimerScreen(
        timers = emptyList(),
        onAddTimer = { _ -> },
        onPauseTimer = { },
        onResumeTimer = { },
        onStopTimer = { },
        onAddOneMinute = { },
        onResetTimer = { }
    )
}