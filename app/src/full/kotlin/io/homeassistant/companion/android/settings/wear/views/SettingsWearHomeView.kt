package io.homeassistant.companion.android.settings.wear.views

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.timoptr.mdiicons.Mdi
import io.github.timoptr.mdiicons.generated.ArrowLeft
import io.github.timoptr.mdiicons.generated.HelpCircleOutline
import io.github.timoptr.mdiicons.rememberImageVector
import io.homeassistant.companion.android.common.R as commonR
import io.homeassistant.companion.android.settings.wear.SettingsWearViewModel
import io.homeassistant.companion.android.util.compose.HomeAssistantAppTheme
import io.homeassistant.companion.android.util.safeTopWindowInsets

const val WEAR_DOCS_LINK = "https://companion.home-assistant.io/docs/wear-os/"

@Composable
fun LoadSettingsHomeView(
    settingsWearViewModel: SettingsWearViewModel,
    deviceName: String,
    loginWearOs: () -> Unit,
    onStartBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeAssistantAppTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = SettingsWearMainView.LANDING,
            modifier = modifier,
        ) {
            composable(SettingsWearMainView.FAVORITES) {
                LoadWearFavoritesSettings(
                    settingsWearViewModel = settingsWearViewModel,
                    onBackClicked = { navController.navigateUp() },
                    events = settingsWearViewModel.resultSnackbar,
                )
            }
            composable(SettingsWearMainView.LANDING) {
                val hasData by settingsWearViewModel.hasData.collectAsState()
                val isAuthenticated by settingsWearViewModel.isAuthenticated.collectAsState()

                SettingWearLandingView(
                    deviceName = deviceName,
                    hasData = hasData,
                    isAuthed = isAuthenticated,
                    navigateFavorites = { navController.navigate(SettingsWearMainView.FAVORITES) },
                    navigateTemplateTile = { navController.navigate(SettingsWearMainView.TEMPLATES) },
                    loginWearOs = loginWearOs,
                    onBackClicked = onStartBackClicked,
                    events = settingsWearViewModel.resultSnackbar,
                )
            }
            composable(SettingsWearMainView.TEMPLATES) {
                SettingsWearTemplateTileList(
                    templateTiles = settingsWearViewModel.templateTiles,
                    onTemplateTileClicked = { tileId ->
                        navController.navigate(SettingsWearMainView.TEMPLATE_TILE.format(tileId))
                    },
                    onBackClicked = {
                        navController.navigateUp()
                    },
                )
            }
            composable(
                route = SettingsWearMainView.TEMPLATE_TILE.format("{tileId}"),
                arguments = listOf(navArgument("tileId") { type = NavType.IntType }),
            ) { backStackEntry ->
                val tileId = backStackEntry.arguments?.getInt("tileId")
                val templateTile = settingsWearViewModel.templateTiles[tileId]
                val renderedTemplate = settingsWearViewModel.templateTilesRenderedTemplates[tileId]

                templateTile?.let {
                    SettingsWearTemplateTile(
                        template = it.template,
                        renderedTemplate = renderedTemplate ?: "",
                        refreshInterval = it.refreshInterval,
                        onContentChanged = { templateContent ->
                            settingsWearViewModel.setTemplateTileContent(tileId!!, templateContent)
                            settingsWearViewModel.sendTemplateTileInfo()
                        },
                        onRefreshIntervalChanged = { refreshInterval ->
                            settingsWearViewModel.setTemplateTileRefreshInterval(tileId!!, refreshInterval)
                            settingsWearViewModel.sendTemplateTileInfo()
                        },
                        onBackClicked = {
                            navController.navigateUp()
                        },
                    )
                }
            }
        }
    }
}

/**
 * Top app bar for the Wear OS settings screens, with a back button and an optional help button.
 *
 * Material 2 [androidx.compose.material.TopAppBar] has a fixed height and cuts a title off once it
 * wraps, so the bar is laid out here instead and grows with its title. This matches the design
 * system bar, [io.homeassistant.companion.android.common.compose.composable.HATopBar], which also
 * grows rather than truncating.
 *
 * @param title Text shown as the title. It wraps and stays fully readable at every font scale.
 */
@Composable
fun SettingsWearTopAppBar(
    title: String,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    docsLink: String? = null,
) {
    val context = LocalContext.current
    Surface(
        color = colorResource(commonR.color.colorBackground),
        contentColor = colorResource(commonR.color.colorOnBackground),
        elevation = AppBarDefaults.TopAppBarElevation,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(safeTopWindowInsets()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AppBarHeight)
                .padding(AppBarDefaults.ContentPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClicked) {
                Image(
                    imageVector = Mdi.ArrowLeft.rememberImageVector(),
                    contentDescription = stringResource(commonR.string.navigate_up),
                    colorFilter = ColorFilter.tint(colorResource(commonR.color.colorOnBackground)),
                )
            }
            Spacer(modifier = Modifier.width(TitleStartGap))
            Text(
                text = title,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.weight(1f),
            )
            if (!docsLink.isNullOrBlank()) {
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, docsLink.toUri())
                        context.startActivity(intent)
                    },
                ) {
                    Image(
                        imageVector = Mdi.HelpCircleOutline.rememberImageVector(),
                        contentDescription = stringResource(commonR.string.help),
                        colorFilter = ColorFilter.tint(colorResource(commonR.color.colorOnBackground)),
                    )
                }
            }
        }
    }
}

/** Minimum height of the bar, mirroring the fixed height Material 2 uses for its top app bar. */
private val AppBarHeight = 56.dp

/**
 * Gap after the navigation icon, placing the title on the same 72dp keyline as the rows below the
 * bar: the 72dp keyline less the 4dp bar padding and the 48dp touch target of the icon button.
 */
private val TitleStartGap = 20.dp
