package ba.fluxor.fetchapi.feature.project_tree.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.feature.project_tree.viewmodel.SubProjectNode

@Composable
fun SubProjectItem(
  node: SubProjectNode,
  onToggle: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onAddFolder: () -> Unit,
  onAddRequest: () -> Unit,
) {
  var showMenu by remember { mutableStateOf(false) }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 2.dp),
  ) {
    Icon(
      imageVector = if (node.expanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
      contentDescription = null,
      modifier = Modifier.size(20.dp),
    )
    Icon(
      imageVector = Icons.Default.Folder,
      contentDescription = null,
      modifier = Modifier.size(16.dp).padding(end = 4.dp),
      tint = MaterialTheme.colorScheme.primary,
    )
    Text(
      text = node.subProject.name,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      modifier = Modifier.weight(1f),
    )
    Box {
      IconButton(onClick = { showMenu = true }, modifier = Modifier.size(20.dp)) {
        Icon(Icons.Default.MoreVert, contentDescription = "Menu", modifier = Modifier.size(16.dp))
      }
      DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        DropdownMenuItem(text = { Text("Add folder") }, onClick = { showMenu = false; onAddFolder() })
        DropdownMenuItem(text = { Text("Add request") }, onClick = { showMenu = false; onAddRequest() })
        DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() })
        DropdownMenuItem(text = { Text("Delete") }, onClick = { showMenu = false; onDelete() })
      }
    }
  }
}
