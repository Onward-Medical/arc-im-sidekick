package com.onwd.arc.im.sidekick

import android.app.Application
import com.onwd.arc.im.sidekick.data.ActiveSensorRepository
import com.onwd.arc.im.sidekick.data.HealthServicesRepository
import com.onwd.arc.im.sidekick.data.JsonFileStore
import com.onwd.arc.im.sidekick.data.PassiveDataRepository
import java.util.Properties

val PERMISSIONS = listOf(
    android.Manifest.permission.BODY_SENSORS,
    android.Manifest.permission.BODY_SENSORS_BACKGROUND,
    android.Manifest.permission.ACTIVITY_RECOGNITION,
    android.Manifest.permission.POST_NOTIFICATIONS
)

class MainApplication : Application() {
    val healthServicesRepository by lazy { HealthServicesRepository(this) }
    val activeSensorRepository by lazy { ActiveSensorRepository(this) }
    val jsonFileStore by lazy { JsonFileStore(this, "data") }
    val passiveDataRepository by lazy { PassiveDataRepository(this) }
    val blobProperties by lazy {
        Properties().apply {
            assets.open("blob.properties").use { load(it) }
        }
    }
}
