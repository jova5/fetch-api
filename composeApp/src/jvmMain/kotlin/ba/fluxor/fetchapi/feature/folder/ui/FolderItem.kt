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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.component.ConfirmDeleteDialog
import ba.fluxor.fetchapi.component.TooltipBelow
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderNode
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun FolderItem(
  node: FolderNode,
  isHovered: Boolean,
  indent: Dp,
  onExpand: () -> Unit,
  onToggle: () -> Unit,
  onDropdownOpen: () -> Unit,
  onDropdownClose: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onAddFolder: () -> Unit,
  onAddRequest: () -> Unit,
) {
  var showMenu by remember { mutableStateOf(false) }
  var showDeleteConfirm by remember { mutableStateOf(false) }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(4.dp))
      .clickable(onClick = onToggle)
      .padding(start = indent, top = 4.dp, bottom = 4.dp, end = 2.dp),
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
        .size(20.dp)
        .padding(start = 2.dp, end = 4.dp),
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
        TooltipBelow(
          text = stringResource(Res.string.more_actions)
        ) {
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
      }
      DropdownMenu(
        expanded = showMenu,
        onDismissRequest = {
          showMenu = false
          onDropdownClose()
        }
      ) {
        DropdownMenuItem(
          text = { Text(stringResource(Res.string.add_folder)) },
          onClick = { showMenu = false; onDropdownClose(); onAddFolder() }
        )
        DropdownMenuItem(
          text = { Text(stringResource(Res.string.add_request)) },
          onClick = { showMenu = false; onDropdownClose(); onAddRequest() }
        )
        DropdownMenuItem(
          text = { Text(stringResource(Res.string.edit)) },
          onClick = { showMenu = false; onDropdownClose(); onEdit() }
        )
        DropdownMenuItem(
          text = { Text(stringResource(Res.string.delete)) },
          onClick = { showMenu = false; onDropdownClose(); showDeleteConfirm = true }
        )
      }
    }
  }

  if (showDeleteConfirm) {
    ConfirmDeleteDialog(
      entityName = node.folder.name,
      onConfirm = onDelete,
      onDismiss = { showDeleteConfirm = false },
    )
  }
}
