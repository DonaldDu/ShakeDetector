package com.dhy.shakedetector

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import kotlin.math.sqrt


class ShakeDetector : LifecycleObserver {
    companion object {
        private var shakeDetector: ShakeDetector? = null
        /**
         * for single instance shake detect, should be called in base activity
         * */
        fun onShake(owner: LifecycleOwner, callback: () -> Unit) {
            if (shakeDetector == null) shakeDetector = ShakeDetector()
            shakeDetector?.onShake(owner, callback)
        }
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var onShakeCallback: () -> Unit
    private var UPDATE_INTERVAL_MS = 0
    private var startCount = 0
    private lateinit var sensor: Sensor
    private lateinit var handler: Handler
    /**
     * shake detect for current instance
     * */
    fun onShake(owner: LifecycleOwner, callback: () -> Unit) {
        if (owner is Context && !::sensorManager.isInitialized) {
            val c = owner as Context
            sensorManager = c.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            UPDATE_INTERVAL_MS = c.getString(R.string.UPDATE_INTERVAL_MS).toInt()
            handler = Handler()
        }

        onShakeCallback = callback
        owner.lifecycle.addObserver(this)
    }

    private var registed = false
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        startCount++
        if (!registed) {
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            registed = true
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        startCount--
        if (startCount == 0) {
            handler.postDelayed({
                sensorManager.unregisterListener(sensorEventListener)
                registed = false
            }, 500)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        if (startCount == 0) shakeDetector = null
    }

    private var lastDate = 0L
    private var x: Float = 0f
    private var y: Float = 0f
    private var z: Float = 0f
    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            x -= event.values[0]
            y -= event.values[1]
            z -= event.values[2]

            val delta = sqrt((x * x + y * y + z * z).toDouble())
            if (delta > 20) {
                if (System.currentTimeMillis() - lastDate > UPDATE_INTERVAL_MS) {
                    lastDate = System.currentTimeMillis()
                    onShakeCallback()
                }
            }

            x = event.values[0]
            y = event.values[1]
            z = event.values[2]
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }
}