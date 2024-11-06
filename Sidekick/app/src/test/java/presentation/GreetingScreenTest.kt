package presentation

import com.example.android.wearable.composestarter.presentation.GreetingScreen
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.google.android.horologist.screenshots.FixedTimeSource
import com.google.android.horologist.screenshots.rng.WearDevice
import com.google.android.horologist.screenshots.rng.WearScreenshotTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class GreetingScreenTest(override val device: WearDevice) : WearScreenshotTest() {
    override val tolerance = 0.02f

    @Test
    fun greetingScreenTest() = runTest {
        AppScaffold(
            timeText = { ResponsiveTimeText(timeSource = FixedTimeSource) }
        ) {
            GreetingScreen(greetingName = "screenshot", onShowList = {})
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun devices() = WearDevice.entries
    }
}
