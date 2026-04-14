package ba.fluxor.fetchapi

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.feature.project.ui.ProjectDropdown
import ba.fluxor.fetchapi.feature.settings.ui.SettingsModal
import ba.fluxor.fetchapi.feature.settings.viewmodel.SettingsViewModel
import ba.fluxor.fetchapi.localization.LocaleProvider
import ba.fluxor.fetchapi.ui.shell.AppLayout
import ba.fluxor.fetchapi.ui.theme.AppTheme
import ba.fluxor.fetchapi.ui.theme.ThemeMode
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.stringResource
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

  val customColors = TitleBarColors(
    background = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F5F5), // TVOJA BOJA
    inactiveBackground = baseStyle.colors.inactiveBackground,
    content = baseStyle.colors.content,
    border = baseStyle.colors.border,
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
    LocalTitleBarStyle provides titleBarStyle
  ) {
    DecoratedWindow(
      onCloseRequest = onCloseRequest
    ) {
      Column(Modifier.fillMaxSize()) {

        TitleBar(Modifier.fillMaxWidth().background(Color.White)) {
          Row(Modifier.align(Alignment.Start)) {
            Text("FetchAPI", Modifier.background(Color.LightGray))
            IconButton(onClick = { showSettings = true }) {
              Icon(Icons.Default.Settings, contentDescription = stringResource(Res.string.settings))
            }
            Box { ProjectDropdown() }
          }
        }
        LocaleProvider(state.language) {
          AppTheme(mode = state.themeMode, scheme = state.colorScheme) {
            AppLayout()
          }
        }
      }

      if (showSettings) {
        SettingsModal(onDismiss = { showSettings = false })
      }
    }
  }
}
