package ba.fluxor.fetchapi.feature.folder.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderNode
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.add_request
import fetchapi.composeapp.generated.resources.delete
import fetchapi.composeapp.generated.resources.edit
import fetchapi.composeapp.generated.resources.menu
import org.jetbrains.compose.resources.stringResource

@Composable
fun FolderItem(
  node: FolderNode,
  onToggle: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onAddRequest: () -> Unit,
) {
  var showMenu by remember { mutableStateOf(false) }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle)
      .padding(start = 24.dp, top = 2.dp, bottom = 2.dp),
  ) {
    Icon(
      imageVector = if (node.expanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
      contentDescription = null,
      modifier = Modifier.size(20.dp),
    )
    Icon(
      imageVector = if (node.expanded) Icons.Default.FolderOpen else Icons.Default.Folder,
      contentDescription = null,
      modifier = Modifier.size(16.dp).padding(end = 4.dp),
      tint = MaterialTheme.colorScheme.secondary,
    )
    Text(
      text = node.folder.name,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.weight(1f),
    )
    Box {
      IconButton(onClick = { showMenu = true }, modifier = Modifier.size(20.dp)) {
        Icon(Icons.Default.MoreVert, contentDescription = stringResource(Res.string.menu),
          modifier = Modifier.size(16.dp))
      }
      DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        DropdownMenuItem(text = { Text(stringResource(Res.string.add_request)) },
          onClick = { showMenu = false; onAddRequest() })
        DropdownMenuItem(text = { Text(stringResource(Res.string.edit)) },
          onClick = { showMenu = false; onEdit() })
        DropdownMenuItem(text = { Text(stringResource(Res.string.delete)) },
          onClick = { showMenu = false; onDelete() })
      }
    }
  }
}
