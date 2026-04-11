package ba.fluxor.fetchapi.ui.shell

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private val MinLeftWidth = 200.dp
private val MaxLeftWidth = 600.dp
private val DividerWidth = 4.dp

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
    Surface(
      color = MaterialTheme.colorScheme.outlineVariant,
      modifier = Modifier
        .width(DividerWidth)
        .fillMaxHeight()
        .draggable(
          state = dragState,
          orientation = Orientation.Horizontal,
        ),
    ) { Box(Modifier.fillMaxSize()) }

    Box(modifier = Modifier.fillMaxSize()) {
      RightTabsPanel()
    }
  }
}
