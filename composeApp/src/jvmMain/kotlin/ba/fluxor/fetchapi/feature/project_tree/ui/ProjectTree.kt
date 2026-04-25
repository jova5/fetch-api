package ba.fluxor.fetchapi.feature.project_tree.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
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
  val state = rememberLazyListState()
  var isHoveringTree by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        awaitPointerEventScope {
          while (true) {
            val event = awaitPointerEvent()
            when (event.type) {
              PointerEventType.Enter -> isHoveringTree = true
              PointerEventType.Exit -> isHoveringTree = false
            }
          }
        }
      }
  ) {

    LazyColumn(
      state = state,
      modifier = Modifier.fillMaxSize().padding(end = 4.dp),
    ) {
      items(
        items = flatItems,
        key = { item ->
          when (item) {
            is TreeItem.SubProjectHeader -> "sp_${item.node.subProject.id}"
            is TreeItem.FolderHeader -> "f_${item.node.folder.id}"
            is TreeItem.RequestLeaf -> "r_${item.request.id}_${item.indent.value}"
          }
        }
      ) { item ->

        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        var isDropdownOpen by remember { mutableStateOf(false) }
        val shouldShowHoverHighlight = isHovered || isDropdownOpen

        val hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource = interactionSource)
            .background(
              color = if (isDropdownOpen) hoverColor else Color.Transparent,
              shape = RoundedCornerShape(4.dp)
            )
        ) {

          when (item) {
            is TreeItem.SubProjectHeader -> SubProjectItem(
              node = item.node,
              isHovered = shouldShowHoverHighlight,
              onExpand = {
                item.node.subProject.id?.let(treeVm::toggleSubProjectExpanded)
              },
              onToggle = {
                item.node.subProject.id?.let(treeVm::toggleSubProjectExpanded)
                tabsVm.openSubProjectTab(item.node.subProject)
              },
              onDropdownOpen = { isDropdownOpen = true },
              onDropdownClose = { isDropdownOpen = false },
              onEdit = { tabsVm.openSubProjectTab(item.node.subProject) },
              onDelete = { item.node.subProject.id?.let(subProjectVm::deleteSubProject) },
              onAddFolder = {
                val spId = item.node.subProject.id ?: return@SubProjectItem
                folderVm.createFolder(spId)
              },
              onAddRequest = {
                val spId = item.node.subProject.id ?: return@SubProjectItem
                requestVm.createRequest(spId, null)
              },
            )

            is TreeItem.FolderHeader -> FolderItem(
              node = item.node,
              isHovered = shouldShowHoverHighlight,
              onExpand = {
                item.node.folder.id?.let(treeVm::toggleFolderExpanded)
              },
              onToggle = {
                item.node.folder.id?.let(treeVm::toggleFolderExpanded)
                tabsVm.openFolderTab(item.node.folder)
              },
              onDropdownOpen = { isDropdownOpen = true },
              onDropdownClose = { isDropdownOpen = false },
              onEdit = { tabsVm.openFolderTab(item.node.folder) },
              onDelete = { item.node.folder.id?.let(folderVm::deleteFolder) },
              onAddRequest = {
                val folderId = item.node.folder.id ?: return@FolderItem
                requestVm.createRequest(item.subProjectId, folderId)
              },
            )

            is TreeItem.RequestLeaf -> RequestItem(
              isHovered = shouldShowHoverHighlight,
              request = item.request,
              indent = item.indent,
              onDropdownOpen = { isDropdownOpen = true },
              onDropdownClose = { isDropdownOpen = false },
              onEdit = { tabsVm.openRequestTab(item.request) },
              onDelete = { item.request.id?.let(requestVm::deleteRequest) },
            )
          }
        }
      }
    }

    if (isHoveringTree) {
      VerticalScrollbar(
        modifier = Modifier
          .width(4.dp)
          .align(Alignment.CenterEnd)
          .fillMaxHeight(),
        adapter = rememberScrollbarAdapter(scrollState = state)
      )
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
    val matchesSp = spNode.subProject.name.lowercase()
      .contains(lower)
    val filteredFolders = spNode.folders.mapNotNull { fNode ->
      val matchesFolder = fNode.folder.name.lowercase()
        .contains(lower)
      val filteredRequests = fNode.requests.filter {
        it.name.lowercase()
          .contains(lower) || it.method.lowercase()
          .contains(lower)
      }
      when {
        matchesFolder -> fNode
        filteredRequests.isNotEmpty() -> fNode.copy(requests = filteredRequests)
        else -> null
      }
    }
    val filteredLoose = spNode.looseRequests.filter {
      it.name.lowercase()
        .contains(lower) || it.method.lowercase()
        .contains(lower)
    }
    when {
      matchesSp -> spNode
      filteredFolders.isNotEmpty() || filteredLoose.isNotEmpty() ->
        spNode.copy(folders = filteredFolders, looseRequests = filteredLoose, expanded = true)

      else -> null
    }
  }
}
