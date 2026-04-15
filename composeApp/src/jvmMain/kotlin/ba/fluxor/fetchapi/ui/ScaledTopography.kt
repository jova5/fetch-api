package ba.fluxor.fetchapi.ui

import androidx.compose.material3.Typography

fun getScaledTypography(baseTypography: Typography, fontScale: Float) =
  Typography(
    displayLarge = baseTypography.displayLarge.copy(
      fontSize = baseTypography.displayLarge.fontSize * fontScale
    ),
    displayMedium = baseTypography.displayMedium.copy(
      fontSize = baseTypography.displayMedium.fontSize * fontScale
    ),
    displaySmall = baseTypography.displaySmall.copy(
      fontSize = baseTypography.displaySmall.fontSize * fontScale
    ),
    headlineLarge = baseTypography.headlineLarge.copy(
      fontSize = baseTypography.headlineLarge.fontSize * fontScale
    ),
    headlineMedium = baseTypography.headlineMedium.copy(
      fontSize = baseTypography.headlineMedium.fontSize * fontScale
    ),
    headlineSmall = baseTypography.headlineSmall.copy(
      fontSize = baseTypography.headlineSmall.fontSize * fontScale
    ),
    titleLarge = baseTypography.titleLarge.copy(
      fontSize = baseTypography.titleLarge.fontSize * fontScale
    ),
    titleMedium = baseTypography.titleMedium.copy(
      fontSize = baseTypography.titleMedium.fontSize * fontScale
    ),
    titleSmall = baseTypography.titleSmall.copy(
      fontSize = baseTypography.titleSmall.fontSize * fontScale
    ),
    bodyLarge = baseTypography.bodyLarge.copy(
      fontSize = baseTypography.bodyLarge.fontSize * fontScale
    ),
    bodyMedium = baseTypography.bodyMedium.copy(
      fontSize = baseTypography.bodyMedium.fontSize * fontScale
    ),
    bodySmall = baseTypography.bodySmall.copy(
      fontSize = baseTypography.bodySmall.fontSize * fontScale
    ),
    labelLarge = baseTypography.labelLarge.copy(
      fontSize = baseTypography.labelLarge.fontSize * fontScale
    ),
    labelMedium = baseTypography.labelMedium.copy(
      fontSize = baseTypography.labelMedium.fontSize * fontScale
    ),
    labelSmall = baseTypography.labelSmall.copy(
      fontSize = baseTypography.labelSmall.fontSize * fontScale
    )
  )
