package ba.fluxor.fetchapi.feature.settings.viewmodel

import ba.fluxor.fetchapi.localization.AppLanguage
import ba.fluxor.fetchapi.ui.theme.AppColorScheme
import ba.fluxor.fetchapi.ui.theme.ThemeMode

data class SettingsUiState(
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val colorScheme: AppColorScheme = AppColorScheme.BLUE,
  val language: AppLanguage = AppLanguage.ENGLISH,
  val fontScale: Float = 1f,
  val loaded: Boolean = false,
  val dividerPercentage: Float = 0f
)
