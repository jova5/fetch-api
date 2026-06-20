package ba.fluxor.fetchapi.feature.project_tree.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
import ba.fluxor.fetchapi.feature.tabs.data.TabType
import ba.fluxor.fetchapi.feature.tabs.viewmodel.FocusTarget
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabsViewModel
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.no_matches
import fetchapi.composeapp.generated.resources.no_sub_projects
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

internal sealed class TreeItem {
  data class SubProjectHeader(val node: SubProjectNode) : TreeItem()
  data class FolderHeader(val node: FolderNode, val subProjectId: Long, val indent: Dp) : TreeItem()
  data class RequestLeaf(val request: Request, val indent: Dp) : TreeItem()
}

/** Stable LazyColumn key for a row; also used to identify the dragged row during drag-and-drop. */
internal fun keyOf(item: TreeItem): String = when (item) {
  is TreeItem.SubProjectHeader -> "sp_${item.node.subProject.id}"
  is TreeItem.FolderHeader -> "f_${item.node.folder.id}"
  is TreeItem.RequestLeaf -> "r_${item.request.id}_${item.indent.value}"
}

@Composable
fun ProjectTree(
  nodes: List<SubProjectNode>,
  searchQuery: String,
  treeVm: ProjectTreeViewModel,
  subProjectVm: SubProjectViewModel,
  folderVm: FolderViewModel,
  requestVm: RequestViewModel,
  tabsVm: TabsViewModel,
) {
  val filtered = if (searchQuery.isBlank()) nodes else filterTree(nodes, searchQuery.trim())

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

  // Drag & drop. Disabled while searching: the flattened list is filtered then, so row indices
  // wouldn't line up with the full tree the ViewModel reorders against.
  val dragState = rememberTreeDragState()
  val dndEnabled = searchQuery.isBlank()
  var autoScrollVelocity by remember { mutableStateOf(0f) }

  fun dispatch(command: DropCommand) {
    when (command) {
      is DropCommand.MoveSubProject ->
        treeVm.moveSubProject(command.draggedId, command.anchorId, command.placeAfter)

      is DropCommand.MoveFolder ->
        treeVm.moveFolder(
          command.draggedId,
          command.targetSubProjectId,
          command.targetParentFolderId,
          command.anchorFolderId,
          command.placeAfter,
        )

      is DropCommand.MoveRequest ->
        treeVm.moveRequest(
          command.draggedId,
          command.targetSubProjectId,
          command.targetFolderId,
          command.anchorRequestId,
          command.placeAfter,
        )
    }
  }

  // Edge auto-scroll while dragging near the top/bottom of the viewport.
  LaunchedEffect(autoScrollVelocity) {
    if (autoScrollVelocity == 0f) return@LaunchedEffect
    while (isActive) {
      state.scrollBy(autoScrollVelocity)
      delay(16.milliseconds)
    }
  }

  val tabsState by tabsVm.state.collectAsStateWithLifecycle()
  val focusTarget = tabsState.selectedTab?.let { FocusTarget(it.type, it.entityId) }

  // Editor-originated focus changes (tab-bar / restore) ask the tree to reveal & scroll to the
  // matching node. Tree clicks don't emit here, so they only update the highlight.
  var pendingScroll by remember { mutableStateOf<FocusTarget?>(null) }
  LaunchedEffect(Unit) {
    tabsVm.focusReveals.collect { target ->
      treeVm.revealEntity(target.type, target.entityId)
      pendingScroll = target
    }
  }
  // Runs after revealEntity rebuilds flatItems, so the target index is computed post-expansion.
  LaunchedEffect(pendingScroll, flatItems) {
    val target = pendingScroll ?: return@LaunchedEffect
    val idx = flatItems.indexOfFirst { it.matches(target) }
    if (idx >= 0) {
      state.scrollToItem(idx)
      pendingScroll = null
    }
  }

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
      itemsIndexed(
        items = flatItems,
        key = { _, item -> keyOf(item) }
      ) { index, item ->

        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        var isDropdownOpen by remember { mutableStateOf(false) }
        val shouldShowHoverHighlight = isHovered || isDropdownOpen

        val hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        val focusColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        val isFocused = item.matches(focusTarget)

        val key = keyOf(item)
        val isBeingDragged = dragState.payload?.key == key
        val dropMode = if (dragState.targetIndex == index) dragState.displayMode else null
        val indicatorColor = MaterialTheme.colorScheme.primary

        val dragModifier = if (dndEnabled) {
          // Key on flatItems too: when the tree reloads after a move the list identity changes, so
          // this restarts and the gesture lambdas re-capture the fresh `item`/`flatItems`. Keying
          // on `key` alone left a stale `flatItems` closed over, which mapped the hit-tested row
          // index into the old list and produced a wrong drop target after the first move.
          Modifier.pointerInput(key, flatItems) {
            detectDragGesturesAfterLongPress(
              onDragStart = { dragState.payload = payloadFor(item, key) },
              onDragEnd = {
                dragState.pendingCommand?.let { dispatch(it) }
                dragState.reset()
                autoScrollVelocity = 0f
              },
              onDragCancel = {
                dragState.reset()
                autoScrollVelocity = 0f
              },
              onDrag = { change, _ ->
                change.consume()
                val info = state.layoutInfo
                val dragged = info.visibleItemsInfo.find { it.key == key }
                if (dragged != null) {
                  val pointerY = dragged.offset + change.position.y
                  updateDropTarget(dragState, info, pointerY, flatItems)
                  autoScrollVelocity = edgeAutoScroll(pointerY, info)
                }
              },
            )
          }
        } else {
          Modifier
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isBeingDragged) 0.4f else 1f)
            .then(dragModifier)
            .hoverable(interactionSource = interactionSource)
            .background(
              color = when {
                isFocused -> focusColor
                isDropdownOpen -> hoverColor
                else -> Color.Transparent
              },
              shape = RoundedCornerShape(4.dp)
            )
            .drawWithContent {
              drawContent()
              val stroke = 2.dp.toPx()
              when (dropMode) {
                DropMode.BEFORE ->
                  drawLine(indicatorColor, Offset(0f, 0f), Offset(size.width, 0f), stroke)

                DropMode.AFTER ->
                  drawLine(indicatorColor, Offset(0f, size.height), Offset(size.width, size.height), stroke)

                DropMode.INTO ->
                  drawRoundRect(indicatorColor, style = Stroke(stroke), cornerRadius = CornerRadius(4.dp.toPx()))

                null -> Unit
              }
            }
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
              indent = item.indent,
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
              onAddFolder = {
                val folderId = item.node.folder.id ?: return@FolderItem
                folderVm.createFolder(item.subProjectId, parentFolderId = folderId)
              },
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
        adapter = rememberScrollbarAdapter(scrollState = state),
        style = LocalScrollbarStyle.current.copy(
          unhoverColor = MaterialTheme.colorScheme.outlineVariant,
          hoverColor = MaterialTheme.colorScheme.primary
        ),
      )
    }
  }
}

private fun TreeItem.matches(focus: FocusTarget?): Boolean {
  if (focus == null) return false
  return when (this) {
    is TreeItem.SubProjectHeader ->
      focus.type == TabType.SUB_PROJECT && node.subProject.id == focus.entityId

    is TreeItem.FolderHeader ->
      focus.type == TabType.FOLDER && node.folder.id == focus.entityId

    is TreeItem.RequestLeaf ->
      focus.type == TabType.REQUEST && request.id == focus.entityId
  }
}

private val FOLDER_BASE_INDENT = 24.dp
private val INDENT_PER_LEVEL = 16.dp
private val REQUEST_EXTRA_INDENT = 24.dp

private fun flattenTree(nodes: List<SubProjectNode>): List<TreeItem> {
  val result = mutableListOf<TreeItem>()
  for (spNode in nodes) {
    result += TreeItem.SubProjectHeader(spNode)
    if (spNode.expanded) {
      for (folderNode in spNode.folders) {
        flattenFolder(folderNode, spNode.subProject.id!!, depth = 0, into = result)
      }
      for (request in spNode.looseRequests) {
        result += TreeItem.RequestLeaf(request, FOLDER_BASE_INDENT)
      }
    }
  }
  return result
}

private fun flattenFolder(
  folderNode: FolderNode,
  subProjectId: Long,
  depth: Int,
  into: MutableList<TreeItem>,
) {
  val folderIndent = FOLDER_BASE_INDENT + INDENT_PER_LEVEL * depth
  into += TreeItem.FolderHeader(folderNode, subProjectId, folderIndent)
  if (folderNode.expanded) {
    for (child in folderNode.subFolders) {
      flattenFolder(child, subProjectId, depth + 1, into)
    }
    for (request in folderNode.requests) {
      into += TreeItem.RequestLeaf(request, folderIndent + REQUEST_EXTRA_INDENT)
    }
  }
}

private fun filterTree(nodes: List<SubProjectNode>, query: String): List<SubProjectNode> {
  val lower = query.lowercase()
  return nodes.mapNotNull { spNode ->
    val matchesSp = spNode.subProject.name.lowercase()
      .contains(lower)
    val filteredFolders = spNode.folders.mapNotNull { filterFolder(it, lower) }
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

/**
 * Keeps a folder if its own name matches (whole subtree retained), or if any descendant folder
 * or request matches (subtree pruned to the matches). Matched folders are force-expanded so the
 * results are visible.
 */
private fun filterFolder(node: FolderNode, lower: String): FolderNode? {
  if (node.folder.name.lowercase().contains(lower)) {
    return node.copy(expanded = true)
  }
  val filteredSub = node.subFolders.mapNotNull { filterFolder(it, lower) }
  val filteredRequests = node.requests.filter {
    it.name.lowercase().contains(lower) || it.method.lowercase().contains(lower)
  }
  return if (filteredSub.isNotEmpty() || filteredRequests.isNotEmpty()) {
    node.copy(subFolders = filteredSub, requests = filteredRequests, expanded = true)
  } else {
    null
  }
}
