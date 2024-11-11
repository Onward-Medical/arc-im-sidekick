package com.onwd.arc.im.sidekick

import android.app.Application
import com.onwd.arc.im.sidekick.data.HealthServicesRepository
import com.onwd.arc.im.sidekick.data.PassiveDataRepository
import com.onwd.arc.im.sidekick.work.PeriodicUploadScheduler

val PERMISSIONS = listOf(
    android.Manifest.permission.BODY_SENSORS,
    android.Manifest.permission.BODY_SENSORS_BACKGROUND,
    android.Manifest.permission.ACTIVITY_RECOGNITION
)

class MainApplication : Application() {
    val healthServicesRepository by lazy { HealthServicesRepository(this) }
    val passiveDataRepository by lazy { PassiveDataRepository(this) }

    override fun onCreate() {
        super.onCreate()
        PeriodicUploadScheduler.scheduleUploadWorker(this)
    }
}
