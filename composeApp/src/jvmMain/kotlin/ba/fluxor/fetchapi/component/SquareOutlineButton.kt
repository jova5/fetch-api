package ba.fluxor.fetchapi.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SquareOutlineButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  borderRadius: Dp = 4.dp,
  enabled: Boolean = true,
  borderWidth: Dp = 0.5.dp,
) {
  val shape = RoundedCornerShape(borderRadius)

  val borderColor = if (enabled) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
  } else {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
  }

  Box(
    modifier = Modifier
      .clip(shape)
      .background(Color.Transparent)
      .then(
        if (borderWidth > 0.dp) {
          Modifier.border(BorderStroke(borderWidth, borderColor), shape)
        } else {
          Modifier
        }
      )
      .clickable(
        enabled = enabled,
        onClick = onClick,
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary)
      )
      .then(modifier),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text
    )
  }
}
