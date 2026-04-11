package ba.fluxor.fetchapi.ui.shell

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.awt.Cursor

private val MinLeftWidth = 200.dp
private val MaxLeftWidth = 600.dp
private val DividerWidth = 2.dp

@Composable
fun MainArea(modifier: Modifier = Modifier) {
  var leftWidth by remember { mutableStateOf(300.dp) }
  val density = LocalDensity.current

  Row(modifier = modifier.fillMaxSize()) {
    Box(modifier = Modifier.width(leftWidth).fillMaxHeight()) {
      LeftTreePanel()
    }

    val dragState = rememberDraggableState { delta ->
      val deltaDp = with(density) { delta.toDp() }
      leftWidth = (leftWidth + deltaDp).coerceIn(MinLeftWidth, MaxLeftWidth)
    }

    Box(
      modifier = Modifier
        .width(10.dp)
        .fillMaxHeight()
        .draggable(
          state = dragState,
          orientation = Orientation.Horizontal,
        )
        .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
    ) {
      Surface(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier
          .width(DividerWidth)
          .fillMaxHeight()
          .align(Alignment.Center)
      ) {}
    }

    Box(modifier = Modifier.fillMaxSize()) {
      RightTabsPanel()
    }
  }
}
