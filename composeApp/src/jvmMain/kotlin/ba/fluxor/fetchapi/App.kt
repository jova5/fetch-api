package ba.fluxor.fetchapi

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.component.SquareIconButton
import ba.fluxor.fetchapi.feature.project.ui.ProjectDropdown
import ba.fluxor.fetchapi.feature.settings.ui.SettingsModal
import ba.fluxor.fetchapi.feature.settings.viewmodel.SettingsViewModel
import ba.fluxor.fetchapi.localization.LocaleProvider
import ba.fluxor.fetchapi.ui.getRobotoFontFamily
import ba.fluxor.fetchapi.ui.getScaledTypography
import ba.fluxor.fetchapi.ui.shell.AppLayout
import ba.fluxor.fetchapi.ui.theme.AppTheme
import ba.fluxor.fetchapi.ui.theme.ThemeMode
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.styling.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
  onCloseRequest: () -> Unit,
) {
  val settingsVm: SettingsViewModel = koinViewModel()
  val state by settingsVm.state.collectAsStateWithLifecycle()
  var showSettings by remember { mutableStateOf(false) }
  val isDark = when (state.themeMode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
  }
  val windowStyle = if (isDark) DecoratedWindowStyle.dark() else DecoratedWindowStyle.light()
  val baseStyle = if (isDark) TitleBarStyle.dark() else TitleBarStyle.light()
  val fontFamily = getRobotoFontFamily()
  val defaultTypography = Typography()
  val scaledTypography = getScaledTypography(defaultTypography, state.fontScale, fontFamily)

  AppTheme(
    mode = state.themeMode,
    scheme = state.colorScheme,
    typography = scaledTypography
  ) {

    val background = MaterialTheme.colorScheme.surfaceContainerLow
    val border = MaterialTheme.colorScheme.primary

    val darkenedBackground = lerp(background, Color.Black, 0.05f)
    val darkenedBorder = lerp(border, Color.Black, 0.3f)

    val customColors = TitleBarColors(
      background = background,
      inactiveBackground = if (isDark) background.copy(alpha = 0.95f) else darkenedBackground,
      content = baseStyle.colors.content,
      border = if (isDark) darkenedBorder else border.copy(alpha = 0.4f),
      fullscreenControlButtonsBackground = baseStyle.colors.fullscreenControlButtonsBackground,
      titlePaneButtonHoveredBackground = baseStyle.colors.titlePaneButtonHoveredBackground,
      titlePaneButtonPressedBackground = baseStyle.colors.titlePaneButtonPressedBackground,
      titlePaneCloseButtonHoveredBackground = baseStyle.colors.titlePaneCloseButtonHoveredBackground,
      titlePaneCloseButtonPressedBackground = baseStyle.colors.titlePaneCloseButtonPressedBackground,
      iconButtonHoveredBackground = baseStyle.colors.iconButtonHoveredBackground,
      iconButtonPressedBackground = baseStyle.colors.iconButtonPressedBackground,
      dropdownPressedBackground = baseStyle.colors.dropdownPressedBackground,
      dropdownHoveredBackground = baseStyle.colors.dropdownHoveredBackground
    )

    val titleBarStyle = TitleBarStyle(
      colors = customColors,
      metrics = baseStyle.metrics,
      icons = baseStyle.icons,
      dropdownStyle = baseStyle.dropdownStyle,
      iconButtonStyle = baseStyle.iconButtonStyle,
      paneButtonStyle = baseStyle.paneButtonStyle,
      paneCloseButtonStyle = baseStyle.paneCloseButtonStyle
    )

    CompositionLocalProvider(
      LocalDecoratedWindowStyle provides windowStyle,
      LocalTitleBarStyle provides titleBarStyle,
      LocalDensity provides Density(
        density = LocalDensity.current.density,
        fontScale = state.fontScale
      )
    ) {
      DecoratedWindow(
        onCloseRequest = onCloseRequest
      ) {
        Column(Modifier.fillMaxSize()) {

          TitleBar(modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
          ) {
            Row(
              modifier = Modifier.align(Alignment.Start),
              horizontalArrangement = Arrangement.Start,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Spacer(Modifier.size(8.dp))
              Text(
                text = "FetchAPI",
                color = MaterialTheme.colorScheme.onBackground
              )
              Spacer(Modifier.size(8.dp))
              CompositionLocalProvider(
                value = LocalRippleConfiguration provides RippleConfiguration(
                  color = MaterialTheme.colorScheme.primary)
              ) {
                SquareIconButton(
                  onClick = { showSettings = true },
                  icon = Icons.Outlined.Settings,
                  borderWidth = 0.dp
                )
                Spacer(Modifier.size(8.dp))
                ProjectDropdown()
              }
            }
          }
          LocaleProvider(state.language) {
            AppLayout()
          }
        }

        if (showSettings) {
          SettingsModal(onDismiss = { showSettings = false })
        }
      }
    }
  }
}
