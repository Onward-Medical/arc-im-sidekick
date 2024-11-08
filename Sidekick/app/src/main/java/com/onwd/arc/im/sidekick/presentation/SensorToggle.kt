package com.onwd.arc.im.sidekick.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.onwd.arc.im.sidekick.R
import com.onwd.arc.im.sidekick.extensions.openSettings

/**
 * A [ToggleChip] for enabling / disabling sensor monitoring.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SensorToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    permissionState: MultiplePermissionsState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    ToggleChip(
        modifier = modifier,
        checked = checked,
        colors = ToggleChipDefaults.toggleChipColors(),
        enabled = !checked,
        onCheckedChange = { enabled ->
            if (permissionState.allPermissionsGranted) {
                onCheckedChange(enabled)
            } else {
                context.openSettings()
            }
        },
        label = { Text(stringResource(id = R.string.sensor_toggle)) },
        toggleControl = {
            Icon(
                imageVector = ToggleChipDefaults.switchIcon(checked),
                contentDescription = stringResource(id = R.string.sensor_toggle)
            )
        }
    )
}
