package com.onwd.arc.im.sidekick.sensors.listener

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.onwd.arc.im.sidekick.sensors.record.ValueAndAccuracyRecord
import es.uji.geotec.backgroundsensors.record.Record
import es.uji.geotec.backgroundsensors.record.accumulator.RecordAccumulator

class ValueAndAccuracySensorListener(
    private val sensor: es.uji.geotec.backgroundsensors.sensor.Sensor,
    private val recordAccumulator: RecordAccumulator<Record>
) :
    SensorEventListener {
    private var accuracy = SensorManager.SENSOR_STATUS_ACCURACY_LOW

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != sensor.type) return

        val value = event.values[0].toInt()

        val record = ValueAndAccuracyRecord(event.timestamp, value, accuracy)
        recordAccumulator.accumulateRecord(record)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        this.accuracy = accuracy
    }
}
