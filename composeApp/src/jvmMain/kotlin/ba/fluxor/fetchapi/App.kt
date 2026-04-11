package ba.fluxor.fetchapi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.feature.project.ui.ProjectScreen
import ba.fluxor.fetchapi.feature.settings.viewmodel.SettingsViewModel
import ba.fluxor.fetchapi.ui.AppTopBar
import ba.fluxor.fetchapi.ui.i18n.LocaleProvider
import ba.fluxor.fetchapi.ui.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun App() {

  val settingsVm: SettingsViewModel = koinViewModel()
  val state by settingsVm.state.collectAsStateWithLifecycle()

  LocaleProvider(state.language) {
    AppTheme(mode = state.themeMode, scheme = state.colorScheme) {
      Scaffold(
        topBar = {
          AppTopBar(
            state = state,
            onThemeModeChange = settingsVm::setThemeMode,
            onColorSchemeChange = settingsVm::setColorScheme,
            onLanguageChange = settingsVm::setLanguage,
          )
        },
      ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
          ProjectScreen()
        }
      }
    }
  }
}
