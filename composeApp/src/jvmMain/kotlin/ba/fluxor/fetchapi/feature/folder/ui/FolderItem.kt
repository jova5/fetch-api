package ba.fluxor.fetchapi.feature.folder.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderNode
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun FolderItem(
  node: FolderNode,
  isHovered: Boolean,
  onExpand: () -> Unit,
  onToggle: () -> Unit,
  onDropdownOpen: () -> Unit,
  onDropdownClose: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onAddRequest: () -> Unit,
) {
  var showMenu by remember { mutableStateOf(false) }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .clickable(onClick = onToggle)
      .padding(start = 24.dp, top = 4.dp, bottom = 4.dp, end = 2.dp),
  ) {
    IconButton(onClick = onExpand, modifier = Modifier.size(20.dp)) {
      Icon(
        imageVector = if (node.expanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
      )
    }
    Icon(
      imageVector = if (node.expanded) Icons.Default.FolderOpen else Icons.Default.Folder,
      contentDescription = null,
      modifier = Modifier
        .size(16.dp)
        .padding(end = 4.dp),
      tint = MaterialTheme.colorScheme.secondary,
    )
    Text(
      text = node.folder.name,
      style = MaterialTheme.typography.bodyMedium,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier
        .weight(1f),
    )
    Box {
      if (isHovered) {
        IconButton(
          onClick = {
            showMenu = true
            onDropdownOpen()
          },
          modifier = Modifier.size(20.dp)
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(Res.string.menu),
            modifier = Modifier.size(16.dp)
          )
        }
      }
      DropdownMenu(
        expanded = showMenu,
        onDismissRequest = {
          showMenu = false
          onDropdownClose()
        }
      ) {
        DropdownMenuItem(
          text = { Text(stringResource(Res.string.add_request)) },
          onClick = { showMenu = false; onAddRequest() }
        )
        DropdownMenuItem(
          text = { Text(stringResource(Res.string.edit)) },
          onClick = { showMenu = false; onEdit() }
        )
        DropdownMenuItem(
          text = { Text(stringResource(Res.string.delete)) },
          onClick = { showMenu = false; onDelete() }
        )
      }
    }
  }
}
