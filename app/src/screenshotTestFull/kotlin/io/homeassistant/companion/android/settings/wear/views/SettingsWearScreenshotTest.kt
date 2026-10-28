package io.homeassistant.companion.android.settings.wear.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import io.homeassistant.companion.android.common.R as commonR
import io.homeassistant.companion.android.common.data.prefs.impl.entities.TemplateTileConfig
import io.homeassistant.companion.android.util.compose.HomeAssistantAppTheme
import kotlinx.coroutines.flow.emptyFlow

/**
 * Renders the Wear OS settings screens at the default and at the largest font scale, where the
 * subheaders and the app bar titles used to be cut off by a fixed height.
 */
class SettingsWearScreenshotTest {

    @PreviewTest
    @Preview(name = "default")
    @Preview(name = "large_font", fontScale = LARGE_FONT_SCALE)
    @Composable
    fun `Wear settings landing`() {
        HomeAssistantAppTheme {
            SettingWearLandingView(
                deviceName = WEAR_DEVICE_NAME,
                hasData = true,
                isAuthed = true,
                navigateFavorites = {},
                navigateTemplateTile = {},
                loginWearOs = {},
                onBackClicked = {},
                events = emptyFlow(),
            )
        }
    }

    @PreviewTest
    @Preview(name = "default")
    @Preview(name = "large_font", fontScale = LARGE_FONT_SCALE)
    @Composable
    fun `Wear template tile list`() {
        HomeAssistantAppTheme {
            SettingsWearTemplateTileList(
                templateTiles = mapOf(
                    123 to TemplateTileConfig("Example entity 1: {{ states('sensor.example_entity_1') }}", 300),
                ),
                onTemplateTileClicked = {},
                onBackClicked = {},
            )
        }
    }

    /**
     * The favorite entities screen builds its content from a ViewModel, so only its app bar, which
     * is the part that was cut off, is rendered here.
     */
    @PreviewTest
    @Preview(name = "default")
    @Preview(name = "large_font", fontScale = LARGE_FONT_SCALE)
    @Composable
    fun `Wear favorite entities top app bar`() {
        HomeAssistantAppTheme {
            SettingsWearTopAppBar(
                title = stringResource(commonR.string.wear_favorite_entities),
                onBackClicked = {},
                docsLink = WEAR_DOCS_LINK,
            )
        }
    }
}

/** Matches the length of the device name in the bug report, which wraps onto a second line. */
private const val WEAR_DEVICE_NAME = "Galaxy Watch Ultra2 (FF6L)"

/** The largest font scale Android offers, which is where the text used to be cut off. */
private const val LARGE_FONT_SCALE = 2f
