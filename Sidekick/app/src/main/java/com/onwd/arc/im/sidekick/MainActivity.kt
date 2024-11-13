package com.onwd.arc.im.sidekick

import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.onwd.arc.im.sidekick.data.MyRecordCallback
import com.onwd.arc.im.sidekick.extensions.checkPermissions
import com.onwd.arc.im.sidekick.presentation.SensorApp
import com.onwd.arc.im.sidekick.work.PeriodicUploadScheduler
import es.uji.geotec.backgroundsensors.collection.CollectionConfiguration
import es.uji.geotec.backgroundsensors.service.manager.ServiceManager
import es.uji.geotec.wearossensors.sensor.WearSensor
import es.uji.geotec.wearossensors.services.WearSensorRecordingService
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private val serviceManager by lazy {
        ServiceManager(
            this,
            WearSensorRecordingService::class.java
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(application as MainApplication) {
            setContent {
                SensorApp(
                    healthServicesRepository = healthServicesRepository,
                    passiveDataRepository = passiveDataRepository
                )
            }
            PeriodicUploadScheduler.scheduleUploadWorker(this)
            // TODO do it only after permission were granted
            // TODO fix foreground service kind on API 35
            collectAccelerometer()
        }
    }

    override fun onResume() {
        super.onResume()
        with(application as MainApplication) {
            runBlocking {
                passiveDataRepository.setPassiveDataEnabled(application.checkPermissions())
            }
        }
    }

    private fun collectAccelerometer() {
        val config = CollectionConfiguration(
            WearSensor.ACCELEROMETER,
            SensorManager.SENSOR_DELAY_NORMAL,
            50
        )
        serviceManager.startCollection(
            config,
            MyRecordCallback((application as MainApplication).jsonFileStore)
        )
    }
}
