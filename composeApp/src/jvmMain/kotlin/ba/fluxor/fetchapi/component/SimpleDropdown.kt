package ba.fluxor.fetchapi.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> SimpleDropdown(
  options: List<T>,
  selected: T,
  onSelect: (T) -> Unit,
  width: Dp = 400.dp,
  height: Dp = 32.dp,
  optionLabel: @Composable (T) -> String,
) {
  var expanded by remember { mutableStateOf(false) }
  var buttonWidth by remember { mutableStateOf(0.dp) }
  val density = LocalDensity.current

  Row(
    modifier = Modifier.padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Box {
      SquareOutlineButton(
        text = optionLabel(selected),
        onClick = { expanded = true },
        modifier = Modifier
          .width(width)
          .height(height)
          .onGloballyPositioned { coordinates ->
            buttonWidth = with(density) { coordinates.size.width.toDp() + 30.dp }
          }
          .padding(start = 8.dp),
        contentAlignment = Alignment.CenterStart,
      )
      DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.width(buttonWidth),
      ) {
        options.forEach { option ->
          DropdownMenuItem(
            text = {
              Text(text = optionLabel(option), maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            onClick = {
              onSelect(option)
              expanded = false
            },
            leadingIcon = {
              if (option == selected) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
              }
            },
          )
        }
      }
    }
  }
}
