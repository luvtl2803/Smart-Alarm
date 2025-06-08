package com.anhq.smartalarm.core.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.core.model.GameDifficulty
import kotlin.math.abs
import kotlin.math.sqrt

class ShakePhoneGame(
    private val context: Context,
    private val difficulty: GameDifficulty
) : AlarmGame(), SensorEventListener {
    override val type = AlarmGameType.SHAKE_PHONE
    override val title = "Lắc điện thoại"
    override val description = "Lắc điện thoại để tắt báo thức"

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var lastX: Float = 0.0f
    private var lastY: Float = 0.0f
    private var lastZ: Float = 0.0f
    
    private val shakeThreshold = when (difficulty) {
        GameDifficulty.EASY -> 600f     // Lắc nhẹ
        GameDifficulty.MEDIUM -> 800f   // Lắc vừa
        GameDifficulty.HARD -> 1000f    // Lắc mạnh
    }
    
    private val requiredShakes = when (difficulty) {
        GameDifficulty.EASY -> 3    // 3 lần lắc
        GameDifficulty.MEDIUM -> 5  // 5 lần lắc
        GameDifficulty.HARD -> 7    // 7 lần lắc
    }
    
    private val minShakeInterval = when (difficulty) {
        GameDifficulty.EASY -> 0L       // Không giới hạn
        GameDifficulty.MEDIUM -> 200L   // 0.2 giây giữa các lần lắc
        GameDifficulty.HARD -> 500L     // 0.5 giây giữa các lần lắc
    }
    
    private var lastShakeTime: Long = 0
    private var shakeCount = 0

    var onShake: ((Int, Int) -> Unit)? = null // Callback khi lắc (số lần đã lắc, tổng số lần cần lắc)

    init {
        setupSensor()
        // Initialize the shake counter display
        onShake?.invoke(shakeCount, requiredShakes)
    }

    private fun setupSensor() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun startListening() {
        sensorManager?.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        // Reset and show initial counter when starting
        shakeCount = 0
        lastShakeTime = 0
        onShake?.invoke(shakeCount, requiredShakes)
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val curTime = System.currentTimeMillis()
                // Chỉ xử lý mỗi 100ms
                if ((curTime - lastUpdate) > 100) {
                    val diffTime = curTime - lastUpdate
                    lastUpdate = curTime

                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]

                    val speed = abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000

                    if (speed > shakeThreshold) {
                        // Kiểm tra khoảng thời gian giữa các lần lắc
                        if (curTime - lastShakeTime > minShakeInterval) {
                            onShake()
                            lastShakeTime = curTime
                        }
                    }

                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }
        }
    }

    private fun onShake() {
        if (isCompleted) return
        
        shakeCount++
        onShake?.invoke(shakeCount, requiredShakes)
        
        if (shakeCount >= requiredShakes) {
            completeGame()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Không cần xử lý
    }

    override fun reset() {
        isCompleted = false
        shakeCount = 0
        lastUpdate = 0
        lastShakeTime = 0
        lastX = 0.0f
        lastY = 0.0f
        lastZ = 0.0f
        // Reset counter display
        onShake?.invoke(shakeCount, requiredShakes)
    }

    // Cleanup
    fun cleanup() {
        stopListening()
        sensorManager = null
        accelerometer = null
    }
} 