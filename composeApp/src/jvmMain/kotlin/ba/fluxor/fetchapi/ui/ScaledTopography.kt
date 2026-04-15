package ba.fluxor.fetchapi.ui

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.Roboto_Regular
import org.jetbrains.compose.resources.Font

@Composable
fun getRobotoFontFamily() = FontFamily(
  Font(Res.font.Roboto_Regular, FontWeight.Normal),
)

fun getScaledTypography(
  baseTypography: Typography,
  fontScale: Float,
  fontFamily: FontFamily
) = Typography(
  displayLarge = baseTypography.displayLarge.copy(
    fontSize = baseTypography.displayLarge.fontSize * fontScale,
    fontFamily = fontFamily
  ),
  displayMedium = baseTypography.displayMedium.copy(
    fontSize = baseTypography.displayMedium.fontSize * fontScale,
    fontFamily = fontFamily
  ),
  displaySmall = baseTypography.displaySmall.copy(
    fontSize = baseTypography.displaySmall.fontSize * fontScale,
    fontFamily = fontFamily
  ),
  headlineLarge = baseTypography.headlineLarge.copy(
    fontSize = baseTypography.headlineLarge.fontSize * fontScale,
    fontFamily = fontFamily
  ),
  headlineMedium = baseTypography.headlineMedium.copy(
    fontSize = baseTypography.headlineMedium.fontSize * fontScale,
    fontFamily = fontFamily
  ),
  headlineSmall = baseTypography.headlineSmall.copy(
    fontSize = baseTypography.headlineSmall.fontSize * fontScale,
    fontFamily = fontFamily
  ),
  titleLarge = baseTypography.titleLarge.copy(
    fontSize = baseTypography.titleLarge.fontSize * fontScale,
    fontFamily = fontFamily
  ),
  titleMedium = baseTypography.titleMedium.copy(
    fontSize = baseTypography.titleMedium.fontSize * fontScale,
    fontFamily = fontFamily
  ),
  titleSmall = baseTypography.titleSmall.copy(
    fontSize = baseTypography.titleSmall.fontSize * fontScale,
    fontFamily = fontFamily
  ),
  bodyLarge = baseTypography.bodyLarge.copy(
    fontSize = baseTypography.bodyLarge.fontSize * fontScale,
    fontFamily = fontFamily
  ),
  bodyMedium = baseTypography.bodyMedium.copy(
    fontSize = baseTypography.bodyMedium.fontSize * fontScale,
    fontFamily = fontFamily
  ),
  bodySmall = baseTypography.bodySmall.copy(
    fontSize = baseTypography.bodySmall.fontSize * fontScale,
    fontFamily = fontFamily
  ),
  labelLarge = baseTypography.labelLarge.copy(
    fontSize = baseTypography.labelLarge.fontSize * fontScale,
    fontFamily = fontFamily
  ),
  labelMedium = baseTypography.labelMedium.copy(
    fontSize = baseTypography.labelMedium.fontSize * fontScale,
    fontFamily = fontFamily
  ),
  labelSmall = baseTypography.labelSmall.copy(
    fontSize = baseTypography.labelSmall.fontSize * fontScale,
    fontFamily = fontFamily
  )
)
