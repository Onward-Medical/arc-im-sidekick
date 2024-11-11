package com.onwd.arc.im.sidekick.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "passive_data")

class PassiveDataRepository(private val context: Context) {
    val passiveDataEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PASSIVE_DATA_ENABLED] ?: false
    }

    suspend fun setPassiveDataEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PASSIVE_DATA_ENABLED] = enabled
        }
    }

    val latestReading: Flow<OffsetDateTime> = getLatestDateTime(LATEST_READING)

    val latestUpload: Flow<OffsetDateTime> = getLatestDateTime(LATEST_UPLOAD)

    suspend fun storeLatestReading(latestReading: OffsetDateTime) {
        storeDateTime(LATEST_READING, latestReading)
    }

    suspend fun storeLatestUpload(latestUpload: OffsetDateTime) {
        storeDateTime(LATEST_UPLOAD, latestUpload)
    }

    private suspend fun storeDateTime(key: Preferences.Key<String>, dateTime: OffsetDateTime) {
        context.dataStore.edit { prefs ->
            prefs[key] = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime)
        }
    }

    private fun getLatestDateTime(key: Preferences.Key<String>): Flow<OffsetDateTime> {
        return context.dataStore.data.map { prefs ->
            prefs[key]
        }.filterNotNull().map { OffsetDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
    }

    companion object {
        private val PASSIVE_DATA_ENABLED = booleanPreferencesKey("passive_data_enabled")
        private val LATEST_READING = stringPreferencesKey("latest_reading")
        private val LATEST_UPLOAD = stringPreferencesKey("latest_upload")
    }
}
