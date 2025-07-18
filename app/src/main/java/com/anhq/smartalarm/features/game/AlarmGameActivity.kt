package com.anhq.smartalarm.features.game

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import com.anhq.smartalarm.core.designsystem.theme.SmartAlarmTheme
import com.anhq.smartalarm.core.utils.AlarmPreviewManager
import com.anhq.smartalarm.core.utils.AlarmReceiver
import com.anhq.smartalarm.core.utils.AlarmSnoozeReceiver
import com.anhq.smartalarm.core.utils.AlarmStopReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmGameActivity : ComponentActivity() {

    @Inject
    lateinit var alarmPreviewManager: AlarmPreviewManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
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

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            @Suppress("DEPRECATION")
            PowerManager.FULL_WAKE_LOCK or @Suppress("DEPRECATION")
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "SmartAlarm:AlarmWakeLock"
        )
        wakeLock.acquire(10 * 60 * 1000L)

        setContent {
            SmartAlarmTheme {
                val viewModel: AlarmGameViewModel = hiltViewModel()

                AlarmGameScreen(
                    viewModel = viewModel,
                    onGameComplete = {
                        if (viewModel.isPreview) {
                            alarmPreviewManager.stopPreview()
                        } else {
                            AlarmReceiver.stopAlarm()
                            val stopIntent = Intent(this, AlarmStopReceiver::class.java).apply {
                                putExtra("alarm_id", viewModel.alarmId)
                            }
                            sendBroadcast(stopIntent)
                        }
                        wakeLock.release()
                        setResult(RESULT_OK)
                        finish()
                    },
                    onSnoozeClick = {
                        if (viewModel.isPreview) {
                            alarmPreviewManager.stopPreview()
                        } else {
                            AlarmReceiver.stopAlarm()
                            val snoozeIntent = Intent(this, AlarmSnoozeReceiver::class.java).apply {
                                putExtra("alarm_id", viewModel.alarmId)
                                putExtra("is_repeating", intent.getBooleanExtra("is_repeating", false))
                                putExtra("snooze_count", intent.getIntExtra("snooze_count", 0))
                            }
                            sendBroadcast(snoozeIntent)
                        }
                        wakeLock.release()
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }
}