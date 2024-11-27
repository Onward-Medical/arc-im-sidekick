package com.onwd.arc.im.sidekick.sensors

import android.content.Context
import android.util.Log
import com.onwd.arc.im.sidekick.sensors.listener.ValueAndAccuracySensorListener
import es.uji.geotec.backgroundsensors.collection.CollectionConfiguration
import es.uji.geotec.backgroundsensors.record.Record
import es.uji.geotec.backgroundsensors.record.accumulator.RecordAccumulator
import es.uji.geotec.backgroundsensors.record.callback.RecordCallback
import es.uji.geotec.backgroundsensors.time.TimeProvider
import es.uji.geotec.wearossensors.collection.WearCollectorManager
import es.uji.geotec.wearossensors.listeners.SensorListenerProvider
import es.uji.geotec.wearossensors.sensor.WearSensor

class ExtendedWearCollectorManager(context: Context) : WearCollectorManager(context) {
    override fun startCollectingFrom(
        collectionConfiguration: CollectionConfiguration,
        recordCallback: RecordCallback<Record>
    ) = when (collectionConfiguration.sensor) {
        WearSensor.HEART_RATE,
        PressureSensor,
        LightSensor,
        TemperatureSensor -> internalStartCollectingFrom(
            collectionConfiguration,
            recordCallback
        )

        is WearSensor -> super.startCollectingFrom(collectionConfiguration, recordCallback)

        else -> false
    }.also {
        val sensorName = collectionConfiguration.sensor.javaClass.simpleName
        Log.d(
            ExtendedWearCollectorManager::class.simpleName,
            "startCollectingFrom $sensorName success: $it"
        )
    }

    private fun internalStartCollectingFrom(
        collectionConfiguration: CollectionConfiguration,
        recordCallback: RecordCallback<Record>
    ): Boolean {
        val sensor = collectionConfiguration.sensor

        if (!sensorManager.isSensorAvailable(sensor)) return false

        val accumulator = RecordAccumulator(
            recordCallback,
            collectionConfiguration.batchSize
        )

        return when (sensor) {
            TemperatureSensor -> {
                val listener = getListenerFor(sensor, accumulator, timeProvider)
                    ?: return false

                listeners[sensor] = listener

                val androidSensor = this.getAndroidSensor(sensor)
                androidSensorManager.registerListener(
                    listener,
                    androidSensor,
                    collectionConfiguration.sensorDelay
                )
            }

            else -> false
        }
    }
}

private fun getListenerFor(
    sensor: es.uji.geotec.backgroundsensors.sensor.Sensor,
    accumulator: RecordAccumulator<Record>,
    timeProvider: TimeProvider
) = when (sensor) {
    WearSensor.HEART_RATE,
    PressureSensor,
    LightSensor,
    TemperatureSensor -> ValueAndAccuracySensorListener(sensor, accumulator)

    is WearSensor -> SensorListenerProvider.getListenerFor(sensor, accumulator, timeProvider)
    else -> null
}
