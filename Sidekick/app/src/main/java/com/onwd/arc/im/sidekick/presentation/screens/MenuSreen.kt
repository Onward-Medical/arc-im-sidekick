package com.onwd.arc.im.sidekick.presentation.screens

import android.annotation.SuppressLint
import android.text.format.DateUtils
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.onwd.arc.im.sidekick.R
import com.onwd.arc.im.sidekick.presentation.SensorToggle
import java.time.OffsetDateTime
import java.util.UUID
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
    latestUpload: OffsetDateTime?,
    userId: String,
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

    @Composable
    fun relativeTimeSpanString(dateTime: OffsetDateTime?): String =
        dateTime?.toInstant()?.toEpochMilli()?.let {
            DateUtils.getRelativeTimeSpanString(
                it,
                currentTime,
                DateUtils.SECOND_IN_MILLIS
            ).toString()
        } ?: stringResource(R.string.never)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SensorToggle(
            checked = enabled,
            onCheckedChange = { enabled -> onEnableClick(enabled) },
            permissionState = permissionState
        )
        Column(
            modifier = Modifier.padding(8.dp).width(120.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Latest reading:")
            Text(relativeTimeSpanString(latestReading))
            Text("Latest upload:")
            Text(relativeTimeSpanString(latestUpload))
            Text("User id: $userId", modifier = Modifier.horizontalScroll(rememberScrollState()))
        }
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
        true,
        OffsetDateTime.now(),
        null,
        UUID.randomUUID().toString(),
        {},
        permissionState = permissionState
    )
}
