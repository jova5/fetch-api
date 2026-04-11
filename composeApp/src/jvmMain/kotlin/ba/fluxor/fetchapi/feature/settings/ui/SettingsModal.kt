package ba.fluxor.fetchapi.feature.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.feature.settings.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsModal(
  onDismiss: () -> Unit,
  settingsVm: SettingsViewModel = koinViewModel(),
) {
  val state by settingsVm.state.collectAsStateWithLifecycle()
  SettingsDialog(
    state = state,
    onThemeModeChange = settingsVm::setThemeMode,
    onColorSchemeChange = settingsVm::setColorScheme,
    onLanguageChange = settingsVm::setLanguage,
    onDismiss = onDismiss,
  )
}
