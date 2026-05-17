package ba.fluxor.fetchapi.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.fluxor.fetchapi.feature.settings.data.SettingRepository
import ba.fluxor.fetchapi.localization.AppLanguage
import ba.fluxor.fetchapi.ui.theme.AppColorScheme
import ba.fluxor.fetchapi.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
  private val repository: SettingRepository,
) : ViewModel() {

  private val _state = MutableStateFlow(SettingsUiState())
  val state: StateFlow<SettingsUiState> = _state.asStateFlow()

  init {
    viewModelScope.launch {
      val mode = repository.getThemeMode()
      val scheme = repository.getColorScheme()
      val language = repository.getLanguage()
      val fontScale = repository.getFontScale()
      val dividerPercentage = repository.getDividerPercentage()
      val requestDividerPercentage = repository.getRequestDividerPercentage()
      _state.update {
        it.copy(
          themeMode = mode ?: ThemeMode.SYSTEM,
          colorScheme = scheme ?: AppColorScheme.BLUE,
          language = language ?: AppLanguage.ENGLISH,
          fontScale = fontScale ?: 1f,
          loaded = true,
          dividerPercentage = dividerPercentage ?: 0f,
          requestDividerPercentage = requestDividerPercentage ?: 0f
        )
      }
    }
  }

  fun setThemeMode(mode: ThemeMode) {
    _state.update { it.copy(themeMode = mode) }
    viewModelScope.launch { repository.setThemeMode(mode) }
  }

  fun setColorScheme(scheme: AppColorScheme) {
    _state.update { it.copy(colorScheme = scheme) }
    viewModelScope.launch { repository.setColorScheme(scheme) }
  }

  fun setLanguage(language: AppLanguage) {
    _state.update { it.copy(language = language) }
    viewModelScope.launch { repository.setLanguage(language) }
  }

  fun setFontScale(fontScale: Float) {
    _state.update { it.copy(fontScale = fontScale) }
    viewModelScope.launch { repository.setFontScale(fontScale) }
  }

  fun setDividerPercentage(dividerPercentage: Float) {
    _state.update { it.copy(dividerPercentage = dividerPercentage) }
    viewModelScope.launch { repository.setDividerPercentage(dividerPercentage) }
  }

  fun setRequestDividerPercentage(dividerPercentage: Float) {
    _state.update { it.copy(requestDividerPercentage = dividerPercentage) }
    viewModelScope.launch { repository.setRequestDividerPercentage(dividerPercentage) }
  }
}
