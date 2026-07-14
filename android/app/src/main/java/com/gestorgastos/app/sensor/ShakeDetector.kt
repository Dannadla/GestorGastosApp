package com.gestorgastos.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Usa el ACELERÓMETRO del dispositivo para detectar cuando el usuario agita
 * el celular ("shake"). Cumple con el requisito de "uso de al menos un
 * sensor" del proyecto: al agitar el teléfono se dispara onShake(), que la
 * UI usa para abrir rápidamente la pantalla de "Agregar gasto".
 */
class ShakeDetector(
    context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTime: Long = 0
    private val shakeThreshold = 12f // sensibilidad del gesto
    private val shakeCooldownMs = 1000L // evita disparos repetidos

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Magnitud de la aceleración menos la gravedad (~9.8 m/s^2)
        val gForce = sqrt((x * x + y * y + z * z).toDouble()).toFloat() - SensorManager.GRAVITY_EARTH

        if (gForce > shakeThreshold) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > shakeCooldownMs) {
                lastShakeTime = now
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No se requiere acción
    }
}
