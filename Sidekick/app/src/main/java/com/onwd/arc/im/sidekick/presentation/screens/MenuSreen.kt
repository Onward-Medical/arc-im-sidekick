package com.onwd.arc.im.sidekick.presentation.screens

import android.annotation.SuppressLint
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.onwd.arc.im.sidekick.presentation.SensorToggle
import java.time.OffsetDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

fun tickerFlow(periodMillis: Long): Flow<Long> = flow {
    while (true) {
        emit(System.currentTimeMillis())
        delay(periodMillis)
    }
}

@SuppressLint("WearRecents")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MenuScreen(
    enabled: Boolean,
    latestReading: OffsetDateTime?,
    onEnableClick: (Boolean) -> Unit,
    permissionState: MultiplePermissionsState
) {

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            tickerFlow(1000).collect {
                currentTime = it
            }
        }
    }

    fun relativeTimeSpanString() =
        DateUtils.getRelativeTimeSpanString(
            latestReading?.toInstant()?.toEpochMilli() ?: 0,
            currentTime,
            DateUtils.SECOND_IN_MILLIS
        )
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        SensorToggle(
            checked = enabled,
            onCheckedChange = { enabled -> onEnableClick(enabled) },
            permissionState = permissionState
        )
        Text("Latest reading:\n${relativeTimeSpanString()}")
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@WearPreviewDevices
@Composable
fun PreviewMenuScreen() {
    val permissionState = object : MultiplePermissionsState {
        override val permissions: List<PermissionState>
            get() = throw NotImplementedError()
        override val shouldShowRationale = false
        override val allPermissionsGranted = true
        override fun launchMultiplePermissionRequest() {
        }

        override val revokedPermissions: List<PermissionState>
            get() = throw NotImplementedError()
    }
    MenuScreen(
        true, OffsetDateTime.now(), {}, permissionState = permissionState
    )
}
