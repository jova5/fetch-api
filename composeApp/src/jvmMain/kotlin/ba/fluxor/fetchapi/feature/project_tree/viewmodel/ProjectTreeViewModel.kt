package ba.fluxor.fetchapi.feature.project_tree.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.fluxor.fetchapi.feature.folder.data.Folder
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderEvents
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderNode
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderViewModel
import ba.fluxor.fetchapi.feature.request.viewmodel.RequestEvents
import ba.fluxor.fetchapi.feature.request.viewmodel.RequestViewModel
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectEvents
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectNode
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectViewModel
import ba.fluxor.fetchapi.feature.tabs.data.TabType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProjectTreeViewModel(
  private val subProjectViewModel: SubProjectViewModel,
  private val folderViewModel: FolderViewModel,
  private val requestViewModel: RequestViewModel
) : ViewModel() {

  private val _state = MutableStateFlow(ProjectTreeUiState())
  val state: StateFlow<ProjectTreeUiState> = _state.asStateFlow()

  private val _refreshEvents =
    merge(
      SubProjectEvents.events,
      FolderEvents.events,
      RequestEvents.events
    )

  init {
    launchCatching {
      _refreshEvents.collect {
        val projectId = _state.value.projectId ?: return@collect
        loadTree(projectId)
      }
    }
  }

  fun loadTree(projectId: Long) {
    launchCatching {
      _state.update { it.copy(projectId = projectId, isLoading = true, error = null) }

      val subProjects = subProjectViewModel.getAllByProjectId(projectId)
      val nodes = subProjects.map { sp ->
        val spId = sp.id!!
        val folders = folderViewModel.getAllBySubProjectId(spId)
        val foldersByParent = folders.groupBy { it.parentFolderId }

        val existing = _state.value.subProjectNodes.find { it.subProject.id == spId }
        val prevExpandedById = existing?.folders
          ?.let { flattenFolderNodes(it) }
          ?.associate { it.folder.id to it.expanded }
          .orEmpty()

        val folderNodes = buildFolderNodes(null, foldersByParent, prevExpandedById)
        val looseRequests = requestViewModel.getAllLooseBySubProjectId(spId)

        SubProjectNode(
          subProject = sp,
          folders = folderNodes,
          looseRequests = looseRequests,
          expanded = existing?.expanded ?: true,
        )
      }

      _state.update { it.copy(subProjectNodes = nodes, isLoading = false) }
    }
  }

  /** Recursively assembles the folder sub-tree under [parentId] from a parent-grouped folder map. */
  private suspend fun buildFolderNodes(
    parentId: Long?,
    foldersByParent: Map<Long?, List<Folder>>,
    prevExpandedById: Map<Long?, Boolean>,
  ): List<FolderNode> {
    return foldersByParent[parentId].orEmpty().map { folder ->
      val requests = requestViewModel.getAllByFolderId(folder.id!!)
      val children = buildFolderNodes(folder.id, foldersByParent, prevExpandedById)
      FolderNode(
        folder = folder,
        requests = requests,
        subFolders = children,
        expanded = prevExpandedById[folder.id] ?: true,
      )
    }
  }

  private fun flattenFolderNodes(nodes: List<FolderNode>): List<FolderNode> {
    val result = mutableListOf<FolderNode>()
    for (node in nodes) {
      result += node
      result += flattenFolderNodes(node.subFolders)
    }
    return result
  }

  fun clearTree() {
    _state.update { ProjectTreeUiState() }
  }

  // --- UI state ---

  fun toggleSubProjectExpanded(id: Long) {
    _state.update { state ->
      state.copy(
        subProjectNodes = state.subProjectNodes.map {
          if (it.subProject.id == id) it.copy(expanded = !it.expanded) else it
        }
      )
    }
  }

  fun toggleFolderExpanded(id: Long) {
    _state.update { state ->
      state.copy(
        subProjectNodes = state.subProjectNodes.map { spNode ->
          spNode.copy(
            folders = mapFolderNodes(spNode.folders) {
              if (it.folder.id == id) it.copy(expanded = !it.expanded) else it
            }
          )
        }
      )
    }
  }

  /** Applies [transform] to every folder node in the tree, recursing through [FolderNode.subFolders]. */
  private fun mapFolderNodes(
    nodes: List<FolderNode>,
    transform: (FolderNode) -> FolderNode,
  ): List<FolderNode> {
    return nodes.map { node ->
      val mapped = transform(node)
      mapped.copy(subFolders = mapFolderNodes(mapped.subFolders, transform))
    }
  }

  /**
   * Force-expands the ancestors of the given entity so it becomes visible in the flattened tree.
   * Set-expand only (never collapses); a no-op for sub-projects, which are already top-level.
   */
  fun revealEntity(type: TabType, entityId: Long) {
    when (type) {
      TabType.SUB_PROJECT -> Unit

      TabType.FOLDER -> revealPath(
        matchesLooseRequest = { false },
        isTargetFolder = { it.folder.id == entityId },
        containsTargetRequest = { false },
      )

      TabType.REQUEST -> revealPath(
        matchesLooseRequest = { sp -> sp.looseRequests.any { it.id == entityId } },
        isTargetFolder = { false },
        containsTargetRequest = { node -> node.requests.any { it.id == entityId } },
      )
    }
  }

  /**
   * Force-expands the sub-project and every ancestor folder on the path to the matching entity so
   * it becomes visible in the flattened tree. Set-expand only (never collapses).
   */
  private fun revealPath(
    matchesLooseRequest: (SubProjectNode) -> Boolean,
    isTargetFolder: (FolderNode) -> Boolean,
    containsTargetRequest: (FolderNode) -> Boolean,
  ) {
    _state.update { state ->
      state.copy(
        subProjectNodes = state.subProjectNodes.map { spNode ->
          val (newFolders, foundInFolders) =
            revealInFolders(spNode.folders, isTargetFolder, containsTargetRequest)
          if (foundInFolders || matchesLooseRequest(spNode)) {
            spNode.copy(folders = newFolders, expanded = true)
          } else {
            spNode.copy(folders = newFolders)
          }
        }
      )
    }
  }

  /**
   * Rebuilds [nodes], expanding every folder that lies on the path to a match. A folder is on the
   * path when a descendant folder matched or it directly holds the target request; the target
   * folder itself is not expanded (only its ancestors). Returns the new list and whether the
   * target was found anywhere within it.
   */
  private fun revealInFolders(
    nodes: List<FolderNode>,
    isTargetFolder: (FolderNode) -> Boolean,
    containsTargetRequest: (FolderNode) -> Boolean,
  ): Pair<List<FolderNode>, Boolean> {
    var found = false
    val mapped = nodes.map { node ->
      val (newChildren, childFound) =
        revealInFolders(node.subFolders, isTargetFolder, containsTargetRequest)
      val selfRequestMatch = containsTargetRequest(node)
      if (childFound || selfRequestMatch || isTargetFolder(node)) found = true
      val onPath = childFound || selfRequestMatch
      node.copy(subFolders = newChildren, expanded = if (onPath) true else node.expanded)
    }
    return mapped to found
  }

  private fun launchCatching(block: suspend () -> Unit) {
    viewModelScope.launch {
      try {
        block()
      } catch (t: Throwable) {
        _state.update { it.copy(isLoading = false, error = t.message ?: t::class.simpleName) }
      }
    }
  }
}
