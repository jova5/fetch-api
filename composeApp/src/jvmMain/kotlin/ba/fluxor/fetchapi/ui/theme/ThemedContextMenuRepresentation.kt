package ba.fluxor.fetchapi.ui.theme

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ContextMenuRepresentation
import androidx.compose.foundation.ContextMenuState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.rememberCursorPositionProvider

/**
 * Theme-aware replacement for Compose's default desktop text context menu (copy / cut /
 * paste / select all). Renders the menu with [MaterialTheme] colors so it follows the
 * active light/dark mode and color scheme instead of the unstyled default.
 */
class ThemedContextMenuRepresentation(
  private val backgroundColor: Color,
  private val textColor: Color,
  private val hoverColor: Color,
  private val borderColor: Color,
  private val shape: Shape,
) : ContextMenuRepresentation {

  @Composable
  override fun Representation(state: ContextMenuState, items: () -> List<ContextMenuItem>) {

    val status = state.status
    if (status !is ContextMenuState.Status.Open) return

    Popup(
      popupPositionProvider = rememberCursorPositionProvider(),
      onDismissRequest = { state.status = ContextMenuState.Status.Closed },
      properties = PopupProperties(focusable = true),
    ) {
      Column(
        modifier = Modifier
          .shadow(8.dp, shape)
          .clip(shape)
          .background(backgroundColor)
          .border(1.dp, borderColor, shape)
          .padding(vertical = 4.dp)
          .width(IntrinsicSize.Max),
      ) {
        items().forEach { item ->
          MenuRow(
            label = item.label,
            onClick = {
              state.status = ContextMenuState.Status.Closed
              item.onClick()
            },
          )
        }
      }
    }
  }

  @Composable
  private fun MenuRow(label: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    Text(
      text = label,
      color = textColor,
      style = MaterialTheme.typography.bodyMedium,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier
        .fillMaxWidth()
        .clickable(
          interactionSource = interaction,
          indication = null,
          onClick = onClick,
        )
        .background(if (hovered) hoverColor else Color.Transparent)
        .padding(horizontal = 16.dp, vertical = 8.dp),
    )
  }
}
