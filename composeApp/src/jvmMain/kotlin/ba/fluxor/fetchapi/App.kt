package ba.fluxor.fetchapi

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.feature.settings.viewmodel.SettingsViewModel
import ba.fluxor.fetchapi.localization.LocaleProvider
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

  val isDark = when (state.themeMode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
  }
  val windowStyle = if (isDark) DecoratedWindowStyle.dark() else DecoratedWindowStyle.light()

  // Prilagođavamo boju TitleBar-a za svaku temu
  val baseStyle = if (isDark) TitleBarStyle.dark() else TitleBarStyle.light()

// Direktno instanciramo implementaciju boja
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

        // Ovo je komponenta koja magično rešava tvoj problem
        TitleBar(Modifier.fillMaxWidth().background(Color.White)) {
          Row(Modifier.align(Alignment.Start)) {
            Text(" Tab 1 ", Modifier.background(Color.LightGray))
            Text(" Tab 2 ", Modifier.background(Color.LightGray))
          }
        }
        LocaleProvider(state.language) {
          AppTheme(mode = state.themeMode, scheme = state.colorScheme) {
            AppLayout()
          }
        }
      }
    }
  }
}
