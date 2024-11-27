package com.onwd.arc.im.sidekick.sensors.record

import com.onwd.arc.im.sidekick.sensors.TemperatureSensor
import es.uji.geotec.backgroundsensors.record.Record

class ValueAndAccuracyRecord(timestamp: Long, val value: Int, val accuracy: Int) : Record(
    TemperatureSensor,
    timestamp
)
