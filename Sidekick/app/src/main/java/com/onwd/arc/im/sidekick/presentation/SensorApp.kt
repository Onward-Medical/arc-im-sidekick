package com.onwd.arc.im.sidekick.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.horologist.compose.layout.AppScaffold
import com.onwd.arc.im.sidekick.PERMISSIONS
import com.onwd.arc.im.sidekick.data.HealthServicesRepository
import com.onwd.arc.im.sidekick.data.PassiveDataRepository
import com.onwd.arc.im.sidekick.presentation.screens.MenuScreen
import com.onwd.arc.im.sidekick.presentation.screens.NotSupportedScreen
import com.onwd.arc.im.sidekick.presentation.theme.WearAppTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SensorApp(
    healthServicesRepository: HealthServicesRepository,
    passiveDataRepository: PassiveDataRepository
) {
    val navController = rememberSwipeDismissableNavController()

    WearAppTheme {
        AppScaffold {
            val viewModel: SensorViewModel = viewModel(
                factory = SensorViewModelFactory(
                    healthServicesRepository = healthServicesRepository,
                    passiveDataRepository = passiveDataRepository
                )
            )
            val latestReading by viewModel.latestReading.collectAsState()
            val latestUpload by viewModel.latestUpload.collectAsState()
            val readingEnabled by viewModel.readingEnabled.collectAsState()
            val userId by viewModel.userId
            val uiState by viewModel.uiState

            if (uiState == UiState.Supported) {
                val permissionState = rememberMultiplePermissionsState(
                    permissions = PERMISSIONS,
                    onPermissionsResult = { permissions ->
                        if (permissions.all { it.value }) {
                            viewModel.toggleEnabled()
                        }
                    }
                )
                SwipeDismissableNavHost(navController = navController, startDestination = "menu") {
                    composable("menu") {
                        MenuScreen(
                            readingEnabled,
                            latestReading,
                            latestUpload,
                            userId,
                            onEnableClick = { viewModel.toggleEnabled() },
                            permissionState
                        )
                    }
                }
            } else if (uiState == UiState.NotSupported) {
                NotSupportedScreen()
            }
        }
    }
}
