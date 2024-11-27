package com.onwd.arc.im.sidekick.data

import android.content.Context
import android.util.Log
import com.onwd.arc.im.sidekick.MainApplication
import com.onwd.arc.im.sidekick.sensors.LightSensor
import com.onwd.arc.im.sidekick.sensors.PressureSensor
import com.onwd.arc.im.sidekick.sensors.TemperatureSensor
import com.onwd.arc.im.sidekick.service.ExtendedWearSensorRecordingService
import es.uji.geotec.backgroundsensors.collection.CollectionConfiguration
import es.uji.geotec.backgroundsensors.sensor.SensorManager
import es.uji.geotec.backgroundsensors.service.manager.ServiceManager
import es.uji.geotec.wearossensors.sensor.WearSensor

class ActiveSensorRepository(private val context: Context) {
    private val sensors = arrayOf(
        WearSensor.ACCELEROMETER,
        WearSensor.GYROSCOPE,
        WearSensor.MAGNETOMETER,
        TemperatureSensor,
        PressureSensor,
        LightSensor
    )
    private val serviceManager = ServiceManager(
        context,
        ExtendedWearSensorRecordingService::class.java
    )
    private val sensorManager = SensorManager(context)

    fun registerForActiveSensors() {
        Log.i(ActiveSensorRepository::class.simpleName, "Registering listener")

        val callback =
            with(context.applicationContext as MainApplication) { MyRecordCallback(jsonFileStore) }
        sensorManager.availableSensors(sensors).apply {
            Log.d(ActiveSensorRepository::class.simpleName, "Available sensors: $this")
        }.forEach { sensor ->
            val config = when (sensor) {
                WearSensor.ACCELEROMETER,
                WearSensor.GYROSCOPE,
                WearSensor.MAGNETOMETER -> CollectionConfiguration(
                    sensor,
                    android.hardware.SensorManager.SENSOR_DELAY_NORMAL,
                    50
                )

                else -> CollectionConfiguration(
                    sensor,
                    android.hardware.SensorManager.SENSOR_DELAY_UI,
                    1
                )
            }
            serviceManager.startCollection(config, callback)
        }
    }

    fun unregisterForActiveSensors() {
        Log.i(ActiveSensorRepository::class.simpleName, "Unregistering listener")
        sensors.forEach { sensor ->
            serviceManager.stopCollection(sensor)
        }
    }
}
