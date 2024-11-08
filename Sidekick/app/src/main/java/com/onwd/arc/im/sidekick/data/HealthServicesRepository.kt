package com.onwd.arc.im.sidekick.data

import android.content.Context
import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.HealthEvent
import androidx.health.services.client.data.PassiveListenerConfig
import com.onwd.arc.im.sidekick.TAG
import com.onwd.arc.im.sidekick.service.DataService

/**
 * Entry point for [HealthServicesClient] APIs. This also provides suspend functions around
 * those APIs to enable use in coroutines.
 */
class HealthServicesRepository(context: Context) {
    private val healthServicesClient = HealthServices.getClient(context)
    private val passiveMonitoringClient = healthServicesClient.passiveMonitoringClient
    private val dataTypes = setOf(
        DataType.STEPS,
        DataType.STEPS_DAILY,
        DataType.CALORIES,
        DataType.CALORIES_DAILY,
        DataType.DISTANCE,
        DataType.DISTANCE_DAILY,
        DataType.FLOORS,
        DataType.FLOORS_DAILY,
        DataType.HEART_RATE_BPM
    )

    private val passiveListenerConfig = PassiveListenerConfig(
        dataTypes = dataTypes,
        shouldUserActivityInfoBeRequested = false,
        dailyGoals = setOf(),
        healthEventTypes = setOf(HealthEvent.Type.FALL_DETECTED)
    )

    suspend fun hasHeartRateCapability(): Boolean {
        val capabilities = passiveMonitoringClient.getCapabilitiesAsync().await()
        return DataType.HEART_RATE_BPM in capabilities.supportedDataTypesPassiveMonitoring
    }

    suspend fun registerForPassiveData() {
        Log.i(TAG, "Registering listener")
        passiveMonitoringClient.setPassiveListenerServiceAsync(
            DataService::class.java,
            passiveListenerConfig
        ).await()
    }

    suspend fun unregisterForPassiveData() {
        Log.i(TAG, "Unregistering listeners")
        passiveMonitoringClient.clearPassiveListenerServiceAsync().await()
    }
}
