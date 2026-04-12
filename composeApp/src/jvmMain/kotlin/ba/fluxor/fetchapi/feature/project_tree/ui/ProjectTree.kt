package ba.fluxor.fetchapi.feature.project_tree.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.feature.project_tree.viewmodel.ProjectTreeViewModel
import ba.fluxor.fetchapi.feature.project_tree.viewmodel.SubProjectNode
import ba.fluxor.fetchapi.feature.request.data.Request

private sealed class TreeItem {
  data class SubProjectHeader(val node: SubProjectNode) : TreeItem()
  data class FolderHeader(val node: ba.fluxor.fetchapi.feature.project_tree.viewmodel.FolderNode, val subProjectId: Long) : TreeItem()
  data class RequestLeaf(val request: Request, val indent: Dp) : TreeItem()
}

@Composable
fun ProjectTree(
  nodes: List<SubProjectNode>,
  query: String,
  treeVm: ProjectTreeViewModel,
) {
  val filtered = if (query.isBlank()) nodes else filterTree(nodes, query.trim())

  if (filtered.isEmpty()) {
    Text(
      text = if (nodes.isEmpty()) "No sub-projects yet" else "No matches",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(8.dp),
    )
    return
  }

  val flatItems = remember(filtered) { flattenTree(filtered) }

  LazyColumn(modifier = Modifier.fillMaxSize()) {
    items(flatItems, key = { item ->
      when (item) {
        is TreeItem.SubProjectHeader -> "sp_${item.node.subProject.id}"
        is TreeItem.FolderHeader -> "f_${item.node.folder.id}"
        is TreeItem.RequestLeaf -> "r_${item.request.id}_${item.indent.value}"
      }
    }) { item ->
      when (item) {
        is TreeItem.SubProjectHeader -> SubProjectItem(
          node = item.node,
          onToggle = { item.node.subProject.id?.let(treeVm::toggleSubProjectExpanded) },
          onEdit = { treeVm.showEditSubProjectDialog(item.node.subProject) },
          onDelete = { item.node.subProject.id?.let(treeVm::deleteSubProject) },
          onAddFolder = { item.node.subProject.id?.let(treeVm::showNewFolderDialog) },
          onAddRequest = { item.node.subProject.id?.let { treeVm.showNewRequestDialog(it, null) } },
        )
        is TreeItem.FolderHeader -> FolderItem(
          node = item.node,
          onToggle = { item.node.folder.id?.let(treeVm::toggleFolderExpanded) },
          onEdit = { treeVm.showEditFolderDialog(item.node.folder) },
          onDelete = { item.node.folder.id?.let(treeVm::deleteFolder) },
          onAddRequest = {
            treeVm.showNewRequestDialog(item.subProjectId, item.node.folder.id)
          },
        )
        is TreeItem.RequestLeaf -> RequestItem(
          request = item.request,
          indent = item.indent,
          onEdit = { treeVm.showEditRequestDialog(item.request) },
          onDelete = { item.request.id?.let(treeVm::deleteRequest) },
        )
      }
    }
  }
}

private fun flattenTree(nodes: List<SubProjectNode>): List<TreeItem> {
  val result = mutableListOf<TreeItem>()
  for (spNode in nodes) {
    result += TreeItem.SubProjectHeader(spNode)
    if (spNode.expanded) {
      for (folderNode in spNode.folders) {
        result += TreeItem.FolderHeader(folderNode, spNode.subProject.id!!)
        if (folderNode.expanded) {
          for (request in folderNode.requests) {
            result += TreeItem.RequestLeaf(request, 48.dp)
          }
        }
      }
      for (request in spNode.looseRequests) {
        result += TreeItem.RequestLeaf(request, 24.dp)
      }
    }
  }
  return result
}

@Composable
private fun SubProjectItem(
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

@Composable
private fun FolderItem(
  node: ba.fluxor.fetchapi.feature.project_tree.viewmodel.FolderNode,
  onToggle: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onAddRequest: () -> Unit,
) {
  var showMenu by remember { mutableStateOf(false) }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(start = 24.dp, top = 2.dp, bottom = 2.dp),
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
        Icon(Icons.Default.MoreVert, contentDescription = "Menu", modifier = Modifier.size(16.dp))
      }
      DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        DropdownMenuItem(text = { Text("Add request") }, onClick = { showMenu = false; onAddRequest() })
        DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() })
        DropdownMenuItem(text = { Text("Delete") }, onClick = { showMenu = false; onDelete() })
      }
    }
  }
}

@Composable
private fun RequestItem(
  request: Request,
  indent: Dp,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  var showMenu by remember { mutableStateOf(false) }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit).padding(start = indent, top = 2.dp, bottom = 2.dp),
  ) {
    Text(
      text = request.method,
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
      color = methodColor(request.method),
      modifier = Modifier.width(40.dp),
    )
    Text(
      text = request.name,
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.weight(1f),
    )
    Box {
      IconButton(onClick = { showMenu = true }, modifier = Modifier.size(20.dp)) {
        Icon(Icons.Default.MoreVert, contentDescription = "Menu", modifier = Modifier.size(16.dp))
      }
      DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() })
        DropdownMenuItem(text = { Text("Delete") }, onClick = { showMenu = false; onDelete() })
      }
    }
  }
}

@Composable
private fun methodColor(method: String): Color = when (method.uppercase()) {
  "GET" -> Color(0xFF4CAF50)
  "POST" -> Color(0xFFFF9800)
  "PUT" -> Color(0xFF2196F3)
  "PATCH" -> Color(0xFF9C27B0)
  "DELETE" -> Color(0xFFF44336)
  "HEAD" -> Color(0xFF607D8B)
  "OPTIONS" -> Color(0xFF795548)
  else -> MaterialTheme.colorScheme.onSurface
}

private fun filterTree(nodes: List<SubProjectNode>, query: String): List<SubProjectNode> {
  val lower = query.lowercase()
  return nodes.mapNotNull { spNode ->
    val matchesSp = spNode.subProject.name.lowercase().contains(lower)
    val filteredFolders = spNode.folders.mapNotNull { fNode ->
      val matchesFolder = fNode.folder.name.lowercase().contains(lower)
      val filteredRequests = fNode.requests.filter {
        it.name.lowercase().contains(lower) || it.method.lowercase().contains(lower)
      }
      when {
        matchesFolder -> fNode
        filteredRequests.isNotEmpty() -> fNode.copy(requests = filteredRequests)
        else -> null
      }
    }
    val filteredLoose = spNode.looseRequests.filter {
      it.name.lowercase().contains(lower) || it.method.lowercase().contains(lower)
    }
    when {
      matchesSp -> spNode
      filteredFolders.isNotEmpty() || filteredLoose.isNotEmpty() ->
        spNode.copy(folders = filteredFolders, looseRequests = filteredLoose, expanded = true)
      else -> null
    }
  }
}
