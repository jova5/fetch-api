package ba.fluxor.fetchapi.feature.tabs.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabItem
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.close_tab
import org.jetbrains.compose.resources.stringResource

@Composable
fun TabBar(
  tabs: List<TabItem>,
  selectedTabId: Long?,
  onSelect: (Long) -> Unit,
  onClose: (Long) -> Unit,
) {
  LazyRow(
    modifier = Modifier.fillMaxWidth()
      .height(36.dp),
  ) {
    items(tabs, key = { it.id }) { tab ->
      TabChip(
        tab = tab,
        selected = tab.id == selectedTabId,
        onSelect = { onSelect(tab.id) },
        onClose = { onClose(tab.id) },
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabChip(
  tab: TabItem,
  selected: Boolean,
  onSelect: () -> Unit,
  onClose: () -> Unit,
) {
  val bg =
    if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxHeight()
      .background(bg)
      .clickable(onClick = onSelect)
      .onClick(
        matcher = PointerMatcher.mouse(PointerButton(2)),
        onClick = onClose
      )
      .padding(start = 12.dp, end = 4.dp),
  ) {
    if (tab.isDirty) {
      Text(
        text = "• ",
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
      )
    }
    Text(
      text = tab.title.ifBlank { "Untitled" },
      style = MaterialTheme.typography.bodySmall,
      color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.width(6.dp))
    IconButton(onClick = onClose, modifier = Modifier.size(20.dp)) {
      Icon(
        Icons.Default.Close,
        contentDescription = stringResource(Res.string.close_tab),
        modifier = Modifier.size(14.dp),
      )
    }
  }
}
