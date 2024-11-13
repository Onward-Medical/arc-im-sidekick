package com.onwd.arc.im.sidekick.data

import com.onwd.arc.im.sidekick.service.toPrimitive
import es.uji.geotec.backgroundsensors.record.Record
import es.uji.geotec.backgroundsensors.record.TriAxialRecord
import es.uji.geotec.backgroundsensors.record.callback.RecordCallback
import es.uji.geotec.wearossensors.records.HeartRateRecord
import es.uji.geotec.wearossensors.records.LocationRecord
import es.uji.geotec.wearossensors.sensor.WearSensor
import java.time.Instant
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class MyRecordCallback(private val store: JsonFileStore) : RecordCallback<Record> {
    override fun onRecordsCollected(records: MutableList<Record>?) {
        MainScope().launch {
            records?.map { record ->
                mapOf(
                    "type" to JsonPrimitive(
                        when (record.sensor) {
                            WearSensor.ACCELEROMETER -> "Accelerometer"
                            WearSensor.GYROSCOPE -> "Gyroscope"
                            WearSensor.MAGNETOMETER -> "Magnetometer"
                            WearSensor.HEART_RATE -> "HeartRate"
                            WearSensor.LOCATION -> "Location"
                            else -> "Unknown"
                        }
                    ),
                    "datetime" to toPrimitive(Instant.ofEpochMilli(record.timestamp))
                ).plus(
                    when (record) {
                        is HeartRateRecord -> mapOf(
                            "value" to JsonPrimitive(record.value),
                            "accuracy" to toPrimitive("Unknown")
                        )

                        is TriAxialRecord -> mapOf(
                            "x" to toPrimitive(record.x),
                            "y" to toPrimitive(record.y),
                            "z" to toPrimitive(record.z)
                        )

                        is LocationRecord -> mapOf(
                            "latitude" to toPrimitive(record.latitude),
                            "longitude" to toPrimitive(record.longitude),
                            "altitude" to toPrimitive(record.altitude),
                            "speed" to toPrimitive(record.speed),
                            "direction" to toPrimitive(record.direction),
                            "horizontalAccuracy" to toPrimitive(record.horizontalAccuracy),
                            "verticalAccuracy" to toPrimitive(record.verticalAccuracy)
                        )

                        else -> throw IllegalArgumentException("Unsupported record type: $record")
                    }
                )
            }?.forEach {
                store.write(
                    JsonObject(it)
                )
            }
        }
    }
}
