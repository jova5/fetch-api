package ba.fluxor.fetchapi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.feature.settings.viewmodel.SettingsViewModel
import ba.fluxor.fetchapi.ui.i18n.LocaleProvider
import ba.fluxor.fetchapi.ui.shell.AppLayout
import ba.fluxor.fetchapi.ui.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FrameWindowScope.App(
  windowState: WindowState,
  onMinimize: () -> Unit,
  onToggleMaximize: () -> Unit,
  onClose: () -> Unit,
) {

  val settingsVm: SettingsViewModel = koinViewModel()
  val state by settingsVm.state.collectAsStateWithLifecycle()

  LocaleProvider(state.language) {
    AppTheme(mode = state.themeMode, scheme = state.colorScheme) {
      AppLayout(
        windowState = windowState,
        onMinimize = onMinimize,
        onToggleMaximize = onToggleMaximize,
        onClose = onClose,
      )
    }
  }
}
