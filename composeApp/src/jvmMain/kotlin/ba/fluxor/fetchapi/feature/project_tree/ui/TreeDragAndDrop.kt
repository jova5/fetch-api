package ba.fluxor.fetchapi.feature.project_tree.ui

import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderNode
import ba.fluxor.fetchapi.feature.request.data.Request

/**
 * Pure drag-and-drop logic for the project tree (no Compose UI). The tree is rendered as a flattened
 * [TreeItem] list, so a drop is described by the *target* row plus a [DropMode] (the third of the row
 * the pointer is over). [resolveDrop] turns that into a validated [DropCommand] that
 * `ProjectTreeViewModel` can apply, or `null` when the drop is not allowed.
 */
internal enum class DropMode { BEFORE, AFTER, INTO }

/** What is currently being dragged. Carries the model needed to resolve and validate a drop. */
internal sealed interface DragPayload {
  val key: String

  data class SubProjectDrag(val id: Long, override val key: String) : DragPayload
  data class FolderDrag(val node: FolderNode, override val key: String) : DragPayload
  data class RequestDrag(val request: Request, override val key: String) : DragPayload
}

/** A validated drop, dispatched to `ProjectTreeViewModel`. */
internal sealed interface DropCommand {
  data class MoveSubProject(
    val draggedId: Long,
    val anchorId: Long,
    val placeAfter: Boolean,
  ) : DropCommand

  data class MoveFolder(
    val draggedId: Long,
    val targetSubProjectId: Long,
    val targetParentFolderId: Long?,
    val anchorFolderId: Long?,
    val placeAfter: Boolean,
  ) : DropCommand

  data class MoveRequest(
    val draggedId: Long,
    val targetSubProjectId: Long,
    val targetFolderId: Long?,
    val anchorRequestId: Long?,
    val placeAfter: Boolean,
  ) : DropCommand
}

/** The command to apply on drop, plus the mode to render as the drop indicator at the target row. */
internal data class DropResolution(val command: DropCommand, val displayMode: DropMode)

/** Mutable drag state hoisted by the tree; drives the drop indicator and the dragged-row styling. */
@Stable
internal class TreeDragState {
  var payload by mutableStateOf<DragPayload?>(null)
  var targetIndex by mutableStateOf(-1)
  var displayMode by mutableStateOf<DropMode?>(null)
  var pendingCommand by mutableStateOf<DropCommand?>(null)

  val isDragging: Boolean get() = payload != null

  fun clearTarget() {
    targetIndex = -1
    displayMode = null
    pendingCommand = null
  }

  fun reset() {
    payload = null
    clearTarget()
  }
}

@Composable
internal fun rememberTreeDragState(): TreeDragState = remember { TreeDragState() }

/** Builds the payload for a row at drag start, or `null` if the row has no persisted id yet. */
internal fun payloadFor(item: TreeItem, key: String): DragPayload? = when (item) {
  is TreeItem.SubProjectHeader -> item.node.subProject.id?.let { DragPayload.SubProjectDrag(it, key) }
  is TreeItem.FolderHeader -> if (item.node.folder.id != null) DragPayload.FolderDrag(item.node, key) else null
  is TreeItem.RequestLeaf -> if (item.request.id != null) DragPayload.RequestDrag(item.request, key) else null
}

/**
 * Hit-tests the pointer (in viewport coordinates) against the visible rows and updates [dragState]
 * with the resolved drop, or clears the target when the drop is invalid / off-list.
 */
internal fun updateDropTarget(
  dragState: TreeDragState,
  info: LazyListLayoutInfo,
  pointerY: Float,
  flatItems: List<TreeItem>,
) {
  val payload = dragState.payload ?: return
  val targetInfo = info.visibleItemsInfo
    .find { pointerY >= it.offset && pointerY < it.offset + it.size }
  val targetItem = targetInfo?.let { flatItems.getOrNull(it.index) }
  if (targetInfo == null || targetItem == null) {
    dragState.clearTarget()
    return
  }
  val rel = if (targetInfo.size == 0) 0.5f else (pointerY - targetInfo.offset) / targetInfo.size
  val mode = when {
    rel < 0.25f -> DropMode.BEFORE
    rel > 0.75f -> DropMode.AFTER
    else -> DropMode.INTO
  }
  val resolution = resolveDrop(payload, targetItem, mode)
  if (resolution == null) {
    dragState.clearTarget()
  } else {
    dragState.targetIndex = targetInfo.index
    dragState.displayMode = resolution.displayMode
    dragState.pendingCommand = resolution.command
  }
}

internal fun resolveDrop(payload: DragPayload, target: TreeItem, mode: DropMode): DropResolution? =
  when (payload) {
    is DragPayload.SubProjectDrag -> resolveSubProjectDrop(payload, target, mode)
    is DragPayload.FolderDrag -> resolveFolderDrop(payload, target, mode)
    is DragPayload.RequestDrag -> resolveRequestDrop(payload, target, mode)
  }

/** Sub-projects only reorder among themselves (before/after another sub-project header). */
private fun resolveSubProjectDrop(
  payload: DragPayload.SubProjectDrag,
  target: TreeItem,
  mode: DropMode,
): DropResolution? {
  if (target !is TreeItem.SubProjectHeader) return null
  val anchorId = target.node.subProject.id ?: return null
  if (anchorId == payload.id) return null
  val placeAfter = mode == DropMode.AFTER
  return DropResolution(
    DropCommand.MoveSubProject(payload.id, anchorId, placeAfter),
    if (placeAfter) DropMode.AFTER else DropMode.BEFORE,
  )
}

private fun resolveFolderDrop(
  payload: DragPayload.FolderDrag,
  target: TreeItem,
  mode: DropMode,
): DropResolution? {
  val draggedId = payload.node.folder.id ?: return null
  return when (target) {
    is TreeItem.SubProjectHeader -> {
      val spId = target.node.subProject.id ?: return null
      DropResolution(
        DropCommand.MoveFolder(draggedId, spId, targetParentFolderId = null, anchorFolderId = null, placeAfter = false),
        DropMode.INTO,
      )
    }

    is TreeItem.FolderHeader -> {
      val folder = target.node.folder
      val folderId = folder.id ?: return null
      // Can't drop a folder into itself or anywhere inside its own subtree.
      if (isFolderInSubtree(folderId, payload.node)) return null
      when (mode) {
        DropMode.INTO -> DropResolution(
          DropCommand.MoveFolder(draggedId, folder.subProjectId, folderId, anchorFolderId = null, placeAfter = false),
          DropMode.INTO,
        )

        else -> {
          val placeAfter = mode == DropMode.AFTER
          // New parent is the anchor's parent; it can't lie inside the dragged subtree either.
          if (folder.parentFolderId != null && isFolderInSubtree(folder.parentFolderId, payload.node)) return null
          DropResolution(
            DropCommand.MoveFolder(draggedId, folder.subProjectId, folder.parentFolderId, folderId, placeAfter),
            if (placeAfter) DropMode.AFTER else DropMode.BEFORE,
          )
        }
      }
    }

    is TreeItem.RequestLeaf -> {
      // Folders and requests are separate ordered groups, so dropping a folder on a request just
      // re-parents it into that request's container (the holding folder, or top-level if loose).
      val request = target.request
      if (request.folderId != null && isFolderInSubtree(request.folderId, payload.node)) return null
      DropResolution(
        DropCommand.MoveFolder(draggedId, request.subProjectId, request.folderId, anchorFolderId = null, placeAfter = false),
        DropMode.INTO,
      )
    }
  }
}

private fun resolveRequestDrop(
  payload: DragPayload.RequestDrag,
  target: TreeItem,
  mode: DropMode,
): DropResolution? {
  val draggedId = payload.request.id ?: return null
  return when (target) {
    is TreeItem.SubProjectHeader -> {
      val spId = target.node.subProject.id ?: return null
      DropResolution(
        DropCommand.MoveRequest(draggedId, spId, targetFolderId = null, anchorRequestId = null, placeAfter = false),
        DropMode.INTO,
      )
    }

    is TreeItem.FolderHeader -> {
      val folder = target.node.folder
      val folderId = folder.id ?: return null
      DropResolution(
        DropCommand.MoveRequest(draggedId, folder.subProjectId, folderId, anchorRequestId = null, placeAfter = false),
        DropMode.INTO,
      )
    }

    is TreeItem.RequestLeaf -> {
      val request = target.request
      val anchorId = request.id ?: return null
      if (anchorId == draggedId) return null
      val placeAfter = mode != DropMode.BEFORE // INTO a leaf behaves like AFTER it
      DropResolution(
        DropCommand.MoveRequest(draggedId, request.subProjectId, request.folderId, anchorId, placeAfter),
        if (placeAfter) DropMode.AFTER else DropMode.BEFORE,
      )
    }
  }
}

private const val AUTO_SCROLL_EDGE_PX = 48f
private const val AUTO_SCROLL_SPEED_PX = 12f

/** Scroll velocity (px/tick) when the pointer is within the top/bottom edge band; 0 otherwise. */
internal fun edgeAutoScroll(pointerY: Float, info: LazyListLayoutInfo): Float = when {
  pointerY < info.viewportStartOffset + AUTO_SCROLL_EDGE_PX -> -AUTO_SCROLL_SPEED_PX
  pointerY > info.viewportEndOffset - AUTO_SCROLL_EDGE_PX -> AUTO_SCROLL_SPEED_PX
  else -> 0f
}

/** True when [candidateId] is [node] itself or any folder nested beneath it. */
private fun isFolderInSubtree(candidateId: Long, node: FolderNode): Boolean {
  if (node.folder.id == candidateId) return true
  return node.subFolders.any { isFolderInSubtree(candidateId, it) }
}
