package com.anhq.smartalarm.features.game

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.anhq.smartalarm.core.designsystem.theme.SmartAlarmTheme
import com.anhq.smartalarm.core.utils.AlarmPreviewManager
import com.anhq.smartalarm.core.utils.AlarmReceiver
import com.anhq.smartalarm.core.utils.SnoozeReceiver
import com.anhq.smartalarm.core.utils.StopReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmGameActivity : ComponentActivity() {

    @Inject
    lateinit var alarmPreviewManager: AlarmPreviewManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent activity from appearing in recent apps
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val tasks = activityManager.appTasks
        if (tasks.isNotEmpty()) {
            tasks[0].setExcludeFromRecents(true)
        }

        // Make the activity full screen and prevent gesture navigation
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )

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
                            val stopIntent = Intent(this, StopReceiver::class.java).apply {
                                putExtra("alarm_id", viewModel.alarmId)
                            }
                            sendBroadcast(stopIntent)
                        }
                        setResult(RESULT_OK)
                        finish()
                    },
                    onSnoozeClick = {
                        if (viewModel.isPreview) {
                            alarmPreviewManager.stopPreview()
                        } else {
                            AlarmReceiver.stopAlarm()
                            val snoozeIntent = Intent(this, SnoozeReceiver::class.java).apply {
                                putExtra("alarm_id", viewModel.alarmId)
                                putExtra("is_repeating", intent.getBooleanExtra("is_repeating", false))
                                putExtra("snooze_count", intent.getIntExtra("snooze_count", 0))
                            }
                            sendBroadcast(snoozeIntent)
                        }
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Re-hide system bars when focus is regained
            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                hide(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // If activity is destroyed without proper completion/snooze, restart it
        if (!isFinishing) {
            val intent = Intent(this, AlarmGameActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtras(getIntent())
            }
            startActivity(intent)
        }
    }
}