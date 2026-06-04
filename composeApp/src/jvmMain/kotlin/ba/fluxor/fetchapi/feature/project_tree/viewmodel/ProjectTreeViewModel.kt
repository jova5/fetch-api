package ba.fluxor.fetchapi.feature.project_tree.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        val folderNodes = folders.map { folder ->
          val requests = requestViewModel.getAllByFolderId(folder.id!!)
          FolderNode(folder = folder, requests = requests)
        }
        val looseRequests = requestViewModel.getAllLooseBySubProjectId(spId)

        val existing = _state.value.subProjectNodes.find { it.subProject.id == spId }
        SubProjectNode(
          subProject = sp,
          folders = folderNodes.map { fn ->
            val existingFolder = existing?.folders?.find { it.folder.id == fn.folder.id }
            fn.copy(expanded = existingFolder?.expanded ?: true)
          },
          looseRequests = looseRequests,
          expanded = existing?.expanded ?: true,
        )
      }

      _state.update { it.copy(subProjectNodes = nodes, isLoading = false) }
    }
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
            folders = spNode.folders.map {
              if (it.folder.id == id) it.copy(expanded = !it.expanded) else it
            }
          )
        }
      )
    }
  }

  /**
   * Force-expands the ancestors of the given entity so it becomes visible in the flattened tree.
   * Set-expand only (never collapses); a no-op for sub-projects, which are already top-level.
   */
  fun revealEntity(type: TabType, entityId: Long) {
    when (type) {
      TabType.SUB_PROJECT -> Unit

      TabType.FOLDER -> _state.update { state ->
        state.copy(
          subProjectNodes = state.subProjectNodes.map { spNode ->
            if (spNode.folders.any { it.folder.id == entityId }) {
              spNode.copy(expanded = true)
            } else {
              spNode
            }
          }
        )
      }

      TabType.REQUEST -> _state.update { state ->
        state.copy(
          subProjectNodes = state.subProjectNodes.map { spNode ->
            val owningFolder = spNode.folders.find { fNode ->
              fNode.requests.any { it.id == entityId }
            }
            val isLoose = spNode.looseRequests.any { it.id == entityId }
            when {
              owningFolder != null -> spNode.copy(
                expanded = true,
                folders = spNode.folders.map {
                  if (it.folder.id == owningFolder.folder.id) it.copy(expanded = true) else it
                },
              )

              isLoose -> spNode.copy(expanded = true)
              else -> spNode
            }
          }
        )
      }
    }
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
