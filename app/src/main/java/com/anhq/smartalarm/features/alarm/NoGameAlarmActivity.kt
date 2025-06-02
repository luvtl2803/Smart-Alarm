package com.anhq.smartalarm.features.alarm

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anhq.smartalarm.core.designsystem.theme.SmartAlarmTheme
import com.anhq.smartalarm.core.utils.AlarmPreviewManager
import com.anhq.smartalarm.core.utils.AlarmReceiver
import com.anhq.smartalarm.core.utils.SnoozeReceiver
import com.anhq.smartalarm.core.utils.StopReceiver
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class NoGameAlarmActivity : ComponentActivity() {

    @Inject
    lateinit var alarmPreviewManager: AlarmPreviewManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val alarmId = intent.getIntExtra("alarm_id", -1)
        val isPreview = intent.getBooleanExtra("is_preview", false)
        val isRepeating = intent.getBooleanExtra("is_repeating", false)
        val snoozeCount = intent.getIntExtra("snooze_count", 0)

        setContent {
            SmartAlarmTheme {
                NoGameAlarmScreen(
                    onStopClick = {
                        if (isPreview) {
                            alarmPreviewManager.stopPreview()
                        } else {
                            AlarmReceiver.stopAlarm()
                            val stopIntent = Intent(this, StopReceiver::class.java).apply {
                                putExtra("alarm_id", alarmId)
                            }
                            sendBroadcast(stopIntent)
                        }
                        setResult(RESULT_OK)
                        finish()
                    },
                    onSnoozeClick = {
                        if (isPreview) {
                            alarmPreviewManager.stopPreview()
                        } else {
                            AlarmReceiver.stopAlarm()
                            val snoozeIntent = Intent(this, SnoozeReceiver::class.java).apply {
                                putExtra("alarm_id", alarmId)
                                putExtra("is_repeating", isRepeating)
                                putExtra("snooze_count", snoozeCount)
                            }
                            sendBroadcast(snoozeIntent)
                        }
                        setResult(RESULT_OK)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun NoGameAlarmScreen(
    onStopClick: () -> Unit,
    onSnoozeClick: () -> Unit
) {
        Column(
            modifier = Modifier
                .fillMaxSize()
            .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        val currentTime = remember {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)
        }

            Text(
            text = currentTime,
            style = MaterialTheme.typography.displayLarge
            )

        Spacer(modifier = Modifier.height(32.dp))

            Row(
            modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onSnoozeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
                ) {
                Text("Tạm hoãn")
                }

                Button(
                    onClick = onStopClick,
                    colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                Text("Dừng")
            }
        }
    }
}

@Preview
@Composable
fun NoGameAlarmPreview() {
    NoGameAlarmScreen(
        {}, {}
    )
}