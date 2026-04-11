package ba.fluxor.fetchapi.ui.shell

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowState
import ba.fluxor.fetchapi.getWorkAreaSize

@Composable
fun FrameWindowScope.TopBar(
  windowState: WindowState,
  onMinimize: () -> Unit,
  onToggleMaximize: () -> Unit,
  onClose: () -> Unit,
) {
  var showSettings by remember { mutableStateOf(false) }

  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant,
    modifier = Modifier.fillMaxWidth().height(40.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      WindowDraggableArea(modifier = Modifier.weight(1f).fillMaxHeight()) {
        Row(
          modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Api,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = "FetchAPI",
            style = MaterialTheme.typography.titleSmall,
          )
          IconButton(onClick = { showSettings = true }) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
          }
          Box { ProjectDropdown() }
          Box(modifier = Modifier.weight(1f).fillMaxHeight())
        }
      }

      val maximized = windowState.size == getWorkAreaSize()

      WindowControlButton(
        icon = Icons.Default.Minimize,
        contentDescription = "Minimize",
        onClick = onMinimize,
      )
      WindowControlButton(
        icon = if (maximized) Icons.Default.FilterNone else Icons.Default.CropSquare,
        contentDescription = if (maximized) "Restore" else "Maximize",
        onClick = onToggleMaximize,
      )
      WindowControlButton(
        icon = Icons.Default.Close,
        contentDescription = "Close",
        onClick = onClose,
      )
    }
  }

  if (showSettings) {
    SettingsModal(onDismiss = { showSettings = false })
  }
}

@Composable
private fun WindowControlButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
) {
  IconButton(
    onClick = onClick,
    modifier = Modifier.size(40.dp),
  ) {
    Icon(imageVector = icon, contentDescription = contentDescription)
  }
}
