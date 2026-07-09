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

fun getAppTypography(
  baseTypography: Typography,
  fontFamily: FontFamily,
) = Typography(
  displayLarge = baseTypography.displayLarge.copy(fontFamily = fontFamily),
  displayMedium = baseTypography.displayMedium.copy(fontFamily = fontFamily),
  displaySmall = baseTypography.displaySmall.copy(fontFamily = fontFamily),
  headlineLarge = baseTypography.headlineLarge.copy(fontFamily = fontFamily),
  headlineMedium = baseTypography.headlineMedium.copy(fontFamily = fontFamily),
  headlineSmall = baseTypography.headlineSmall.copy(fontFamily = fontFamily),
  titleLarge = baseTypography.titleLarge.copy(fontFamily = fontFamily),
  titleMedium = baseTypography.titleMedium.copy(fontFamily = fontFamily),
  titleSmall = baseTypography.titleSmall.copy(fontFamily = fontFamily),
  bodyLarge = baseTypography.bodyLarge.copy(fontFamily = fontFamily),
  bodyMedium = baseTypography.bodyMedium.copy(fontFamily = fontFamily),
  bodySmall = baseTypography.bodySmall.copy(fontFamily = fontFamily),
  labelLarge = baseTypography.labelLarge.copy(fontFamily = fontFamily),
  labelMedium = baseTypography.labelMedium.copy(fontFamily = fontFamily),
  labelSmall = baseTypography.labelSmall.copy(fontFamily = fontFamily),
)
