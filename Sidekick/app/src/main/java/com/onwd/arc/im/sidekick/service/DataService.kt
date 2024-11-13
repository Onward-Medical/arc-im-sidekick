package com.onwd.arc.im.sidekick.service

import android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY
import android.os.BatteryManager.BATTERY_PROPERTY_STATUS
import android.os.SystemClock
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.HealthEvent
import androidx.health.services.client.data.HeartRateAccuracy
import androidx.health.services.client.data.LocationAccuracy
import androidx.health.services.client.data.UserActivityInfo
import com.onwd.arc.im.sidekick.MainApplication
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun toPrimitive(value: Any?): JsonElement = when (value) {
    is Float -> JsonPrimitive(value)
    is Int -> JsonPrimitive(value)
    is Long -> JsonPrimitive(value)
    is Double -> JsonPrimitive(value)
    is String -> JsonPrimitive(value)
    is Boolean -> JsonPrimitive(value)
    is Instant -> toPrimitive(OffsetDateTime.ofInstant(value, ZoneId.systemDefault()))
    is OffsetDateTime -> JsonPrimitive(value.format(ISO_OFFSET_DATE_TIME))
    is HeartRateAccuracy -> JsonPrimitive(value.sensorStatus.name)
    is LocationAccuracy -> JsonObject(
        mapOf(
            "horizontalPositionErrorMeters" to JsonPrimitive(value.horizontalPositionErrorMeters),
            "verticalPositionErrorMeters" to JsonPrimitive(value.verticalPositionErrorMeters)
        )
    )

    null -> JsonNull
    else -> throw IllegalArgumentException("Unsupported value type: $value")
}

/**
 * Service to receive data from Health Services.
 *
 * Passive data is delivered from Health Services to this service. Override the appropriate methods
 * in [PassiveListenerService] to receive updates for new data points, goals achieved etc.
 */
class DataService : PassiveListenerService() {
    private val repository
        get() = (application as MainApplication).passiveDataRepository
    private val store
        get() = (application as MainApplication).jsonFileStore

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        MainScope().launch {
            repository.storeLatestReading(OffsetDateTime.now())
            listOf(
                dataPoints.sampleDataPoints.map {
                    JsonObject(
                        mapOf(
                            "type" to JsonPrimitive(it.dataType.name),
                            "value" to toPrimitive(it.value),
                            "datetime" to toPrimitive(
                                it.getTimeInstant(
                                    Instant.ofEpochMilli(
                                        System.currentTimeMillis() - SystemClock.elapsedRealtime()
                                    )
                                )
                            ),
                            "accuracy" to toPrimitive(it.accuracy)
                        )
                    )
                },
                dataPoints.intervalDataPoints.map {
                    JsonObject(
                        mapOf(
                            "type" to JsonPrimitive(it.dataType.name),
                            "value" to toPrimitive(it.value),
                            "startTime" to toPrimitive(
                                it.getStartInstant(
                                    Instant.ofEpochMilli(
                                        System.currentTimeMillis() - SystemClock.elapsedRealtime()
                                    )
                                )
                            ),
                            "endTime" to toPrimitive(
                                it.getEndInstant(
                                    Instant.ofEpochMilli(
                                        System.currentTimeMillis() - SystemClock.elapsedRealtime()
                                    )
                                )
                            ),
                            "accuracy" to toPrimitive(it.accuracy)
                        )
                    )
                }
            ).flatten().forEach {
                store.write(it)
            }
            writeBattery()
        }
    }

    override fun onUserActivityInfoReceived(info: UserActivityInfo) {
        MainScope().launch {
            store.write(
                JsonObject(
                    mapOf(
                        "type" to JsonPrimitive(info::class.simpleName),
                        "datetime" to toPrimitive(info.stateChangeTime),
                        "value" to JsonPrimitive(info.userActivityState.name)
                    )
                )
            )
        }
    }

    override fun onHealthEventReceived(event: HealthEvent) {
        if (event.type == HealthEvent.Type.FALL_DETECTED) {
            MainScope().launch {
                store.write(
                    JsonObject(
                        mapOf(
                            "type" to JsonPrimitive(event::class.simpleName),
                            "datetime" to toPrimitive(event.eventTime),
                            "value" to JsonPrimitive(event.type.name)
                        )
                    )
                )
            }
        }
    }

    override fun onPermissionLost() {
        MainScope().launch {
            repository.setPassiveDataEnabled(false)
        }
    }

    private suspend fun writeBattery() {
        with(applicationContext.getSystemService(BATTERY_SERVICE) as android.os.BatteryManager) {
            val batteryLevel = getIntProperty(BATTERY_PROPERTY_CAPACITY)
            val batteryStatus = getIntProperty(BATTERY_PROPERTY_STATUS)
            store.write(
                JsonObject(
                    mapOf(
                        "type" to JsonPrimitive("BatteryState"),
                        "datetime" to toPrimitive(OffsetDateTime.now()),
                        "value" to JsonPrimitive(batteryLevel),
                        "status" to when (batteryStatus) {
                            android.os.BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                            android.os.BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                            android.os.BatteryManager.BATTERY_STATUS_FULL -> "Full"
                            android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                            android.os.BatteryManager.BATTERY_STATUS_UNKNOWN -> "Unknown"
                            else -> "Invalid"
                        }.let { JsonPrimitive(it) }
                    )
                )
            )
        }
    }
}
