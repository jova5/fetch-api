package ba.fluxor.fetchapi.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.feature.settings.viewmodel.SettingsUiState
import ba.fluxor.fetchapi.ui.i18n.AppLanguage
import ba.fluxor.fetchapi.ui.theme.AppColorScheme
import ba.fluxor.fetchapi.ui.theme.ThemeMode
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsDialog(
  state: SettingsUiState,
  onThemeModeChange: (ThemeMode) -> Unit,
  onColorSchemeChange: (AppColorScheme) -> Unit,
  onLanguageChange: (AppLanguage) -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.settings)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Section(Res.string.theme_mode) {
          ThemeMode.entries.forEach { mode ->
            RadioRow(
              selected = state.themeMode == mode,
              label = stringResource(mode.labelRes()),
              onClick = { onThemeModeChange(mode) },
            )
          }
        }
        Section(Res.string.color_scheme) {
          AppColorScheme.entries.forEach { scheme ->
            RadioRow(
              selected = state.colorScheme == scheme,
              label = stringResource(scheme.labelRes()),
              onClick = { onColorSchemeChange(scheme) },
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
        Section(Res.string.language) {
          AppLanguage.entries.forEach { lang ->
            RadioRow(
              selected = state.language == lang,
              label = stringResource(lang.labelRes()),
              onClick = { onLanguageChange(lang) },
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(Res.string.close)) }
    },
  )
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
