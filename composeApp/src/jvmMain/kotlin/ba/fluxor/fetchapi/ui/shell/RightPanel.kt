package ba.fluxor.fetchapi.ui.shell

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RightTabsPanel() {
  Column(modifier = Modifier.fillMaxSize()) {
    Box(
      modifier = Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 12.dp),
      contentAlignment = Alignment.CenterStart,
    ) {
      Text(
        text = "No open tabs",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    HorizontalDivider()
    Box(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "Editor area",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
