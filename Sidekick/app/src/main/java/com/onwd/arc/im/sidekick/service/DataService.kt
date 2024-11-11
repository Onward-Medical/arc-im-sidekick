package com.onwd.arc.im.sidekick.service

import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.HealthEvent
import androidx.health.services.client.data.UserActivityInfo
import androidx.health.services.client.data.UserActivityState
import com.onwd.arc.im.sidekick.data.PassiveDataRepository
import java.time.OffsetDateTime
import kotlinx.coroutines.runBlocking

/**
 * Service to receive data from Health Services.
 *
 * Passive data is delivered from Health Services to this service. Override the appropriate methods
 * in [PassiveListenerService] to receive updates for new data points, goals achieved etc.
 */
class DataService : PassiveListenerService() {
    private val repository = PassiveDataRepository(this)

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        runBlocking {
            repository.storeLatestReading(OffsetDateTime.now())
        }
    }

    override fun onUserActivityInfoReceived(info: UserActivityInfo) {
        val stateChangeTime = info.stateChangeTime
        val userActivityState = info.userActivityState
        if (userActivityState == UserActivityState.USER_ACTIVITY_ASLEEP) {
            // ...
        }
    }

    override fun onHealthEventReceived(event: HealthEvent) {
        if (event.type == HealthEvent.Type.FALL_DETECTED) {
            // ...
        }
    }

    override fun onPermissionLost() {
        runBlocking {
            repository.setPassiveDataEnabled(false)
        }
    }
}
