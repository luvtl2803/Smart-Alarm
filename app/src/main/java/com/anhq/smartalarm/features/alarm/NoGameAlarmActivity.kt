package com.anhq.smartalarm.features.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anhq.smartalarm.core.designsystem.theme.SmartAlarmTheme
import com.anhq.smartalarm.core.designsystem.theme.label2
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
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }

        // Acquire wake lock
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "SmartAlarm:AlarmWakeLock"
        )
        wakeLock.acquire(10 * 60 * 1000L) // 10 minutes

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
                        wakeLock.release()
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
                        wakeLock.release()
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

            Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    onClick = onSnoozeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
                ) {
                    Text("Tạm hoãn", style = label2)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    onClick = onStopClick,
                    colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Dừng", style = label2)
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