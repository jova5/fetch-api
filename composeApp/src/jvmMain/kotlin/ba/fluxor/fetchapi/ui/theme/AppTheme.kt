package ba.fluxor.fetchapi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
  mode: ThemeMode,
  scheme: AppColorScheme,
  typography: Typography,
  content: @Composable () -> Unit,
) {

  val isDark = when (mode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
  }

  MaterialTheme(
    typography = typography,
    colorScheme = colorSchemeFor(scheme, isDark),
    content = content,
  )
}
