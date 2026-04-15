package ba.fluxor.fetchapi.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.feature.settings.viewmodel.SettingsViewModel
import ba.fluxor.fetchapi.localization.AppLanguage
import ba.fluxor.fetchapi.ui.theme.AppColorScheme
import ba.fluxor.fetchapi.ui.theme.ThemeMode
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsModal(
  onDismiss: () -> Unit,
  settingsVm: SettingsViewModel = koinViewModel(),
) {
  val state by settingsVm.state.collectAsStateWithLifecycle()

  Dialog(
    onDismissRequest = onDismiss,
  ) {
    Surface {
      Column(modifier = Modifier.padding(24.dp)) {
        Text(
          text = stringResource(Res.string.settings),
          style = MaterialTheme.typography.headlineSmall,
          modifier = Modifier.padding(bottom = 16.dp),
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Section(Res.string.theme_mode) {
            ThemeMode.entries.forEach { mode ->
              RadioRow(
                selected = state.themeMode == mode,
                label = stringResource(mode.labelRes()),
                onClick = { settingsVm.setThemeMode(mode) },
              )
            }
          }
          Section(Res.string.color_scheme) {
            AppColorScheme.entries.forEach { scheme ->
              RadioRow(
                selected = state.colorScheme == scheme,
                label = stringResource(scheme.labelRes()),
                onClick = { settingsVm.setColorScheme(scheme) },
                leading = {
                  Box(
                    Modifier
                      .size(16.dp)
                      .clip(CircleShape)
                      .background(scheme.seed),
                  )
                },
              )
            }
          }

          Section(Res.string.font_size) {
            Slider(
              value = state.fontScale,
              onValueChange = { newValue ->
                settingsVm.setFontScale(newValue)
              },
              valueRange = 0.8f..2.5f,
              steps = 7,
              modifier = Modifier.fillMaxWidth()
            )
            val formattedValue = "%.1f".format(state.fontScale)
            Text(
              text = stringResource(Res.string.font_size_format, formattedValue),
              style = MaterialTheme.typography.labelLarge
            )
          }

          Section(Res.string.language) {
            AppLanguage.entries.forEach { lang ->
              RadioRow(
                selected = state.language == lang,
                label = stringResource(lang.labelRes()),
                onClick = { settingsVm.setLanguage(lang) },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun Section(title: StringResource, content: @Composable () -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(stringResource(title), style = MaterialTheme.typography.titleSmall)
    content()
  }
}

@Composable
private fun RadioRow(
  selected: Boolean,
  label: String,
  onClick: () -> Unit,
  leading: (@Composable () -> Unit)? = null,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    RadioButton(selected = selected, onClick = onClick)
    if (leading != null) leading()
    Text(label)
  }
}

private fun ThemeMode.labelRes(): StringResource = when (this) {
  ThemeMode.LIGHT -> Res.string.theme_light
  ThemeMode.DARK -> Res.string.theme_dark
  ThemeMode.SYSTEM -> Res.string.theme_system
}

private fun AppColorScheme.labelRes(): StringResource = when (this) {
  AppColorScheme.GREEN -> Res.string.color_green
  AppColorScheme.BLUE -> Res.string.color_blue
  AppColorScheme.RED -> Res.string.color_red
  AppColorScheme.ORANGE -> Res.string.color_orange
}

private fun AppLanguage.labelRes(): StringResource = when (this) {
  AppLanguage.ENGLISH -> Res.string.language_english
  AppLanguage.SERBIAN -> Res.string.language_serbian
}
