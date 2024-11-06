package presentation

import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.example.android.wearable.composestarter.presentation.ListScreen
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.google.android.horologist.screenshots.FixedTimeSource
import com.google.android.horologist.screenshots.rng.WearDevice
import com.google.android.horologist.screenshots.rng.WearScreenshotTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class ListScreenTest(override val device: WearDevice) : WearScreenshotTest() {
    override val tolerance = 0.03f

    @Test
    fun listScreenTest() {
        runTest {
            AppScaffold(
                timeText = { ResponsiveTimeText(timeSource = FixedTimeSource) }
            ) {
                ListScreen()
            }
        }

        composeRule.onNode(hasScrollToIndexAction())
            .performScrollToIndex(3)
            .performTouchInput {
                swipeUp()
            }

        captureScreenshot("_end")
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun devices() = WearDevice.entries
    }
}
