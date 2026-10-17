package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepSensorManager(
    private val context: Context,
    private val onStepDetected: (Int) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    val isHardwareSensorAvailable: Boolean
        get() = stepDetectorSensor != null || stepCounterSensor != null

    private var initialCounterValue: Float? = null

    fun startListening() {
        if (stepDetectorSensor != null) {
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values.isNotEmpty() && event.values[0] == 1.0f) {
                onStepDetected(1)
            }
        } else if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0]
            if (initialCounterValue == null) {
                initialCounterValue = totalSteps
            } else {
                val delta = (totalSteps - (initialCounterValue ?: totalSteps)).toInt()
                if (delta > 0) {
                    onStepDetected(delta)
                    initialCounterValue = totalSteps
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
