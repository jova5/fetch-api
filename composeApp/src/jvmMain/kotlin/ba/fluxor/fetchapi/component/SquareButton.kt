package ba.fluxor.fetchapi.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SquareButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  borderRadius: Dp = 4.dp,
  enabled: Boolean = true,
) {
  val shape = RoundedCornerShape(borderRadius)

  // Boja pozadine zavisi od toga da li je dugme omogućeno
  val containerColor = if (enabled) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
  }

  // Boja teksta se prilagođava pozadini (npr. bijela na plavoj)
  val contentColor = if (enabled) {
    MaterialTheme.colorScheme.onPrimary
  } else {
    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
  }

  Box(
    modifier = Modifier
      .clip(shape)
      .background(containerColor) // Puna boja umjesto Transparent
      .clickable(
        enabled = enabled,
        onClick = onClick,
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true, color = contentColor) // Ripple u boji teksta
      )
      .then(modifier),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = contentColor,
      style = MaterialTheme.typography.labelLarge,
    )
  }
}
