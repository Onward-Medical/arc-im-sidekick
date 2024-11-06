package presentation

import com.example.android.wearable.composestarter.presentation.SampleDialogContent
import com.google.android.horologist.screenshots.rng.WearDevice
import com.google.android.horologist.screenshots.rng.WearScreenshotTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class SampleDialogTest(override val device: WearDevice) : WearScreenshotTest() {
    override val tolerance = 0.02f

    @Test
    fun greetingScreenTest() = runTest {
        SampleDialogContent(onCancel = { }, onDismiss = { }, onOk = {})
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun devices() = WearDevice.entries
    }
}
