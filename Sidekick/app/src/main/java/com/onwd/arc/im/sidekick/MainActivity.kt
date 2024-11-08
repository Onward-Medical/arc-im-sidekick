package com.onwd.arc.im.sidekick

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.onwd.arc.im.sidekick.extensions.checkPermissions
import com.onwd.arc.im.sidekick.presentation.SensorApp
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(application as MainApplication) {
            setContent {
                SensorApp(
                    healthServicesRepository = healthServicesRepository,
                    passiveDataRepository = passiveDataRepository
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        with(application as MainApplication) {
            runBlocking {
                passiveDataRepository.setPassiveDataEnabled(application.checkPermissions())
            }
        }
    }
}
