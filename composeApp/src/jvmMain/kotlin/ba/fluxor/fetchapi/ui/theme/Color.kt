package ba.fluxor.fetchapi.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private data class SchemePair(val light: ColorScheme, val dark: ColorScheme)

private val green = SchemePair(
  light = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7E4B9),
    onPrimaryContainer = Color(0xFF002106),
    secondary = Color(0xFF4F6352),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2E8D2),
    tertiary = Color(0xFF386569),
    tertiaryContainer = Color(0xFFBCEBEF),
  ),
  dark = darkColorScheme(
    primary = Color(0xFF9CD49F),
    onPrimary = Color(0xFF003910),
    primaryContainer = Color(0xFF12531C),
    onPrimaryContainer = Color(0xFFB7F0B9),
    secondary = Color(0xFFB6CCB7),
    secondaryContainer = Color(0xFF374B3B),
    tertiary = Color(0xFFA0CFD3),
    tertiaryContainer = Color(0xFF1F4D51),
  ),
)

private val blue = SchemePair(
  light = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4FF),
    onPrimaryContainer = Color(0xFF001C3B),
    secondary = Color(0xFF545F71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8E3F8),
    tertiary = Color(0xFF6D5677),
    tertiaryContainer = Color(0xFFF6D9FF),
  ),
  dark = darkColorScheme(
    primary = Color(0xFFA6C8FF),
    onPrimary = Color(0xFF003063),
    primaryContainer = Color(0xFF00468A),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = Color(0xFFBCC7DC),
    secondaryContainer = Color(0xFF3C4758),
    tertiary = Color(0xFFD9BDE2),
    tertiaryContainer = Color(0xFF553F5F),
  ),
)

private val red = SchemePair(
  light = lightColorScheme(
    primary = Color(0xFFB3261E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF775655),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD6),
    tertiary = Color(0xFF755B3E),
    tertiaryContainer = Color(0xFFFFDDB8),
  ),
  dark = darkColorScheme(
    primary = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFE7BDBA),
    secondaryContainer = Color(0xFF5D3F3E),
    tertiary = Color(0xFFE6C28C),
    tertiaryContainer = Color(0xFF5B4327),
  ),
)

private val orange = SchemePair(
  light = lightColorScheme(
    primary = Color(0xFFEF6C00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCC2),
    onPrimaryContainer = Color(0xFF2D1600),
    secondary = Color(0xFF745943),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC2),
    tertiary = Color(0xFF5B6236),
    tertiaryContainer = Color(0xFFDFE8B0),
  ),
  dark = darkColorScheme(
    primary = Color(0xFFFFB77C),
    onPrimary = Color(0xFF4A2800),
    primaryContainer = Color(0xFF693D00),
    onPrimaryContainer = Color(0xFFFFDCC2),
    secondary = Color(0xFFE3C0A2),
    secondaryContainer = Color(0xFF5A422C),
    tertiary = Color(0xFFC3CC96),
    tertiaryContainer = Color(0xFF434A20),
  ),
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
