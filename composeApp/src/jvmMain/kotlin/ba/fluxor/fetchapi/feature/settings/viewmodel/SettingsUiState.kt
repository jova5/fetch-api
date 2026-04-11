package ba.fluxor.fetchapi.feature.settings.viewmodel

import ba.fluxor.fetchapi.ui.i18n.AppLanguage
import ba.fluxor.fetchapi.ui.theme.AppColorScheme
import ba.fluxor.fetchapi.ui.theme.ThemeMode

data class SettingsUiState(
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val colorScheme: AppColorScheme = AppColorScheme.BLUE,
  val language: AppLanguage = AppLanguage.ENGLISH,
  val loaded: Boolean = false,
)
