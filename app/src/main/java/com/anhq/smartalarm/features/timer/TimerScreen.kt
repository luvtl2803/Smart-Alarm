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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.anhq.smartalarm.core.designsystem.theme.title2
import com.anhq.smartalarm.core.model.Timer
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.duration.DurationDialog
import com.maxkeppeler.sheets.duration.models.DurationConfig
import com.maxkeppeler.sheets.duration.models.DurationFormat
import com.maxkeppeler.sheets.duration.models.DurationSelection
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
    var showAddDialog by remember { mutableStateOf(false) }

    val durationState = rememberUseCaseState(
        visible = showAddDialog,
        onDismissRequest = { showAddDialog = false }
    )

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
                    showAddDialog = true
                    durationState.show()
                },
                shape = RoundedCornerShape(50.dp)
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
                        style = title2,
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

        DurationDialog(
            state = durationState,
            selection = DurationSelection(
                onNegativeClick = { showAddDialog = false },
                onPositiveClick = { timeInSeconds ->
                    val totalMillis = timeInSeconds * 1000L
                    onAddTimer(totalMillis)
                    showAddDialog = false
                }
            ),
            config = DurationConfig(
                timeFormat = DurationFormat.HH_MM_SS,
                minTime = 1,
                maxTime = 24 * 60 * 60,
                displayClearButton = false
            )
        )
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
