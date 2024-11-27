package com.onwd.arc.im.sidekick.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.onwd.arc.im.sidekick.data.ActiveSensorRepository
import com.onwd.arc.im.sidekick.data.HealthServicesRepository
import com.onwd.arc.im.sidekick.data.PassiveDataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SensorViewModel(
    private val healthServicesRepository: HealthServicesRepository,
    private val activeSensorRepository: ActiveSensorRepository,
    private val passiveDataRepository: PassiveDataRepository
) : ViewModel() {
    // Provides a hot flow of the latest HR value read from Data Store whilst there is an active
    // UI subscription. HR values are written to the Data Store in the [PassiveDataService] each
    // time an update is provided by Health Services.
    val latestReading = passiveDataRepository.latestReading
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val latestUpload = passiveDataRepository.latestUpload
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val readingEnabled = passiveDataRepository.passiveDataEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val userId = mutableStateOf("")

    val uiState: MutableState<UiState> = mutableStateOf(UiState.Startup)

    init {
        viewModelScope.launch {
            val supported = healthServicesRepository.hasHeartRateCapability()
            uiState.value = if (supported) {
                UiState.Supported
            } else {
                UiState.NotSupported
            }
            userId.value = passiveDataRepository.getUserId()
        }

        viewModelScope.launch {
            passiveDataRepository.passiveDataEnabled.distinctUntilChanged().collect { enabled ->
                if (enabled) {
                    healthServicesRepository.registerForPassiveData()
                    activeSensorRepository.registerForActiveSensors()
                } else {
                    healthServicesRepository.unregisterForPassiveData()
                    activeSensorRepository.unregisterForActiveSensors()
                }
            }
        }
    }

    fun toggleEnabled() {
        viewModelScope.launch {
            val newEnabledStatus = !readingEnabled.value
            passiveDataRepository.setPassiveDataEnabled(newEnabledStatus)
        }
    }
}

class SensorViewModelFactory(
    private val healthServicesRepository: HealthServicesRepository,
    private val activeSensorRepository: ActiveSensorRepository,
    private val passiveDataRepository: PassiveDataRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SensorViewModel(
                healthServicesRepository = healthServicesRepository,
                activeSensorRepository = activeSensorRepository,
                passiveDataRepository = passiveDataRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class UiState {
    object Startup : UiState()
    object NotSupported : UiState()
    object Supported : UiState()
}
