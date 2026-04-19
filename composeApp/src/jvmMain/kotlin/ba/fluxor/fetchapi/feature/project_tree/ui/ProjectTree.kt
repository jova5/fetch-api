package ba.fluxor.fetchapi.feature.project_tree.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.feature.folder.ui.FolderItem
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderNode
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderViewModel
import ba.fluxor.fetchapi.feature.project_tree.viewmodel.ProjectTreeViewModel
import ba.fluxor.fetchapi.feature.request.data.Request
import ba.fluxor.fetchapi.feature.request.ui.RequestItem
import ba.fluxor.fetchapi.feature.request.viewmodel.RequestViewModel
import ba.fluxor.fetchapi.feature.sub_project.ui.SubProjectItem
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectNode
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectViewModel
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabsViewModel
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.no_matches
import fetchapi.composeapp.generated.resources.no_sub_projects
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private sealed class TreeItem {
  data class SubProjectHeader(val node: SubProjectNode) : TreeItem()
  data class FolderHeader(val node: FolderNode, val subProjectId: Long) : TreeItem()
  data class RequestLeaf(val request: Request, val indent: Dp) : TreeItem()
}

@Composable
fun ProjectTree(
  nodes: List<SubProjectNode>,
  query: String,
  treeVm: ProjectTreeViewModel,
  subProjectVm: SubProjectViewModel,
  folderVm: FolderViewModel,
  requestVm: RequestViewModel,
  tabsVm: TabsViewModel,
) {
  val filtered = if (query.isBlank()) nodes else filterTree(nodes, query.trim())
  val scope = rememberCoroutineScope()

  if (filtered.isEmpty()) {
    Text(
      text = if (nodes.isEmpty()) stringResource(Res.string.no_sub_projects) else stringResource(
        Res.string.no_matches),
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
          onToggle = {
            item.node.subProject.id?.let(treeVm::toggleSubProjectExpanded)
            tabsVm.openSubProjectTab(item.node.subProject)
          },
          onEdit = { tabsVm.openSubProjectTab(item.node.subProject) },
          onDelete = { item.node.subProject.id?.let(subProjectVm::deleteSubProject) },
          onAddFolder = {
            val spId = item.node.subProject.id ?: return@SubProjectItem
            folderVm.createFolderWithDefaultName(spId)
          },
          onAddRequest = {
            val spId = item.node.subProject.id ?: return@SubProjectItem
            scope.launch {
              val request = requestVm.createRequestWithDefaultName(spId, null)
              tabsVm.openRequestTab(request)
            }
          },
        )

        is TreeItem.FolderHeader -> FolderItem(
          node = item.node,
          onToggle = {
            item.node.folder.id?.let(treeVm::toggleFolderExpanded)
            tabsVm.openFolderTab(item.node.folder)
          },
          onEdit = { tabsVm.openFolderTab(item.node.folder) },
          onDelete = { item.node.folder.id?.let(folderVm::deleteFolder) },
          onAddRequest = {
            val folderId = item.node.folder.id ?: return@FolderItem
            scope.launch {
              val request = requestVm.createRequestWithDefaultName(item.subProjectId, folderId)
              tabsVm.openRequestTab(request)
            }
          },
        )

        is TreeItem.RequestLeaf -> RequestItem(
          request = item.request,
          indent = item.indent,
          onEdit = { tabsVm.openRequestTab(item.request) },
          onDelete = { item.request.id?.let(requestVm::deleteRequest) },
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
