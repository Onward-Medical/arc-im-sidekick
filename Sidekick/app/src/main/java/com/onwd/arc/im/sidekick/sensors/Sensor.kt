package com.onwd.arc.im.sidekick.sensors

import android.content.pm.PackageManager
import android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE
import android.hardware.Sensor.TYPE_LIGHT
import android.hardware.Sensor.TYPE_PRESSURE
import es.uji.geotec.backgroundsensors.sensor.Sensor

object TemperatureSensor : Sensor {
    override fun getType(): Int = TYPE_AMBIENT_TEMPERATURE

    override fun getSystemFeature(): String {
        return PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE
    }
}

object PressureSensor : Sensor {
    override fun getType(): Int = TYPE_PRESSURE

    override fun getSystemFeature(): String {
        return PackageManager.FEATURE_SENSOR_BAROMETER
    }
}

object LightSensor : Sensor {
    override fun getType(): Int = TYPE_LIGHT

    override fun getSystemFeature(): String {
        return PackageManager.FEATURE_SENSOR_LIGHT
    }
}
