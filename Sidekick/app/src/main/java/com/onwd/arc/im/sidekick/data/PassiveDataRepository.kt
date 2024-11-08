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

    val latestReading: Flow<OffsetDateTime> = context.dataStore.data.map { prefs ->
        prefs[LATEST_READING]
    }.filterNotNull().map { OffsetDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }

    suspend fun storeLatestReading(latestReading: OffsetDateTime) {
        context.dataStore.edit { prefs ->
            prefs[LATEST_READING] = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(latestReading)
        }
    }

    companion object {
        private val PASSIVE_DATA_ENABLED = booleanPreferencesKey("passive_data_enabled")
        private val LATEST_READING = stringPreferencesKey("latest_reading")
    }
}
