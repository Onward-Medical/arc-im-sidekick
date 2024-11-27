package com.onwd.arc.im.sidekick.service

import com.onwd.arc.im.sidekick.sensors.ExtendedWearCollectorManager
import es.uji.geotec.backgroundsensors.collection.CollectorManager
import es.uji.geotec.wearossensors.services.WearSensorRecordingService

class ExtendedWearSensorRecordingService : WearSensorRecordingService() {
    override fun getCollectorManager(): CollectorManager {
        return ExtendedWearCollectorManager(this)
    }
}
