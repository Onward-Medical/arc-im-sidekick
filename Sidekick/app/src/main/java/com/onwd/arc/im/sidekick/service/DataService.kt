package com.onwd.arc.im.sidekick.service

import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
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
}
