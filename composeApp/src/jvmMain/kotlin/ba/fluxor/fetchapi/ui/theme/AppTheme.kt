package ba.fluxor.fetchapi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

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

  val shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(4.dp),
    extraLarge = RoundedCornerShape(4.dp)
  )

  MaterialTheme(
    typography = typography,
    colorScheme = colorSchemeFor(scheme, isDark),
    content = content,
    shapes = shapes
  )
}
