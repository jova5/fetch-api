package ba.fluxor.fetchapi.ui.theme

import androidx.compose.material3.ColorScheme
import ba.fluxor.fetchapi.ui.theme.blue.blueDarkScheme
import ba.fluxor.fetchapi.ui.theme.blue.blueLightScheme
import ba.fluxor.fetchapi.ui.theme.green.greenDarkScheme
import ba.fluxor.fetchapi.ui.theme.green.greenLightScheme
import ba.fluxor.fetchapi.ui.theme.orange.orangeDarkScheme
import ba.fluxor.fetchapi.ui.theme.orange.orangeLightScheme
import ba.fluxor.fetchapi.ui.theme.red.redDarkScheme
import ba.fluxor.fetchapi.ui.theme.red.redLightScheme

private data class SchemePair(val light: ColorScheme, val dark: ColorScheme)

private val green = SchemePair(
  light = greenLightScheme,
  dark = greenDarkScheme,
)

private val blue = SchemePair(
  light = blueLightScheme,
  dark = blueDarkScheme,
)

private val red = SchemePair(
  light = redLightScheme,
  dark = redDarkScheme,
)

private val orange = SchemePair(
  light = orangeLightScheme,
  dark = orangeDarkScheme,
)

internal fun colorSchemeFor(scheme: AppColorScheme, dark: Boolean): ColorScheme {
  val pair = when (scheme) {
    AppColorScheme.GREEN -> green
    AppColorScheme.BLUE -> blue
    AppColorScheme.RED -> red
    AppColorScheme.ORANGE -> orange
  }
  return if (dark) pair.dark else pair.light
}
