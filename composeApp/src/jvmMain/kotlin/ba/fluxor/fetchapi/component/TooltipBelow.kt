package ba.fluxor.fetchapi.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipBelow(
  text: String,
  content: @Composable () -> Unit
) {
  TooltipBox(
    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
      positioning = TooltipAnchorPosition.Below,
      spacingBetweenTooltipAndAnchor = 4.dp
    ),
    tooltip = {
      PlainTooltip {
        Text(text = text)
      }
    },
    state = rememberTooltipState()
  ) {
    content()
  }
}
