package ba.fluxor.fetchapi.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import ba.fluxor.fetchapi.feature.settings.ui.SettingsDialog
import ba.fluxor.fetchapi.feature.settings.viewmodel.SettingsUiState
import ba.fluxor.fetchapi.ui.i18n.AppLanguage
import ba.fluxor.fetchapi.ui.theme.AppColorScheme
import ba.fluxor.fetchapi.ui.theme.ThemeMode
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.app_title
import fetchapi.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppTopBar(
  state: SettingsUiState,
  onThemeModeChange: (ThemeMode) -> Unit,
  onColorSchemeChange: (AppColorScheme) -> Unit,
  onLanguageChange: (AppLanguage) -> Unit,
) {
  var showSettings by remember { mutableStateOf(false) }

  TopAppBar(
    title = { Text(stringResource(Res.string.app_title)) },
    actions = {
      TextButton(onClick = { showSettings = true }) {
        Text(stringResource(Res.string.settings))
      }
    },
  )

  if (showSettings) {
    SettingsDialog(
      state = state,
      onThemeModeChange = onThemeModeChange,
      onColorSchemeChange = onColorSchemeChange,
      onLanguageChange = onLanguageChange,
      onDismiss = { showSettings = false },
    )
  }
}
