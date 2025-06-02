package com.anhq.smartalarm.core.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.anhq.smartalarm.core.model.AlarmGameType
import kotlin.math.abs
import kotlin.math.sqrt

class ShakePhoneGame(private val context: Context) : AlarmGame(), SensorEventListener {
    override val type = AlarmGameType.SHAKE_PHONE
    override val title = "Shake Phone"
    override val description = "Shake your phone to stop the alarm"

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var lastX: Float = 0.0f
    private var lastY: Float = 0.0f
    private var lastZ: Float = 0.0f
    
    private val shakeThreshold = 800f // Ngưỡng để xác định lắc
    private val requiredShakes = 5 // Số lần lắc cần thiết
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
                        onShake()
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