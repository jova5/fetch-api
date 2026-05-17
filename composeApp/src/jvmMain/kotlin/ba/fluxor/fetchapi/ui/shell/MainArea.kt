package ba.fluxor.fetchapi.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.LocalWindowWidth
import ba.fluxor.fetchapi.feature.project_tree.ui.ProjectTreeViewPanel
import ba.fluxor.fetchapi.feature.settings.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Cursor

@Composable
fun MainArea(
  modifier: Modifier = Modifier,
  settingsVm: SettingsViewModel = koinViewModel()
) {
  val settingState by settingsVm.state.collectAsStateWithLifecycle()

  val density = LocalDensity.current
  val focusManager = LocalFocusManager.current
  val windowWidth = LocalWindowWidth.current

  val minLeftWidth = 225.dp
  val maxLeftWidth = windowWidth * 0.50f

  var percentage by remember { mutableStateOf(settingState.dividerPercentage) }

  LaunchedEffect(settingState.dividerPercentage) {
    percentage = settingState.dividerPercentage
  }

  val leftWidth by remember(windowWidth, percentage) {
    derivedStateOf {
      (minLeftWidth + (maxLeftWidth - minLeftWidth) * percentage)
        .coerceIn(minLeftWidth, maxLeftWidth)
    }
  }

  Row(modifier = modifier
    .fillMaxSize()
    .pointerInput(Unit) {
      detectTapGestures { focusManager.clearFocus() }
    }
  ) {
    Box(
      modifier = Modifier
        .width(leftWidth)
        .fillMaxHeight()
        .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
      ProjectTreeViewPanel()
    }
    var virtualMouseX by remember { mutableStateOf(leftWidth) }

    val dragState = rememberDraggableState { delta ->
      val deltaDp = with(density) { delta.toDp() }

      virtualMouseX += deltaDp

      if (virtualMouseX in minLeftWidth..maxLeftWidth) {
        val range = maxLeftWidth - minLeftWidth
        if (range > 0.dp) {
          percentage = ((virtualMouseX - minLeftWidth) / range).coerceIn(0f, 1f)
        }
      }
    }

    Box(
      modifier = Modifier
        .fillMaxHeight()
        .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
        .draggable(
          state = dragState,
          orientation = Orientation.Horizontal,
          onDragStarted = {
            virtualMouseX = leftWidth
          },
          onDragStopped = {
            settingsVm.setDividerPercentage(percentage)
          }
        )
    ) {
      Row {
        Column(
          modifier = Modifier
            .width(4.dp)
            .fillMaxHeight()
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow))
        {}
        Column(
          modifier = Modifier
            .width(2.dp)
            .fillMaxHeight()
            .background(color = MaterialTheme.colorScheme.outlineVariant))
        {}
        Column(
          modifier = Modifier
            .width(4.dp)
            .fillMaxHeight()) {}
      }
    }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(color = MaterialTheme.colorScheme.surface)
    ) {
      RightTabsPanel()
    }
  }
}
