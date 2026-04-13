package ba.fluxor.fetchapi.feature.project_tree.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.fluxor.fetchapi.feature.folder.data.FolderRepository
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderEvents
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderNode
import ba.fluxor.fetchapi.feature.request.data.RequestRepository
import ba.fluxor.fetchapi.feature.request.viewmodel.RequestEvents
import ba.fluxor.fetchapi.feature.sub_project.data.SubProjectRepository
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectEvents
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectNode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProjectTreeViewModel(
  private val subProjectRepository: SubProjectRepository,
  private val folderRepository: FolderRepository,
  private val requestRepository: RequestRepository,
) : ViewModel() {

  private val _state = MutableStateFlow(ProjectTreeUiState())
  val state: StateFlow<ProjectTreeUiState> = _state.asStateFlow()

  private val _refreshEvents =
    merge(SubProjectEvents.refreshEvent, FolderEvents.refreshEvent, RequestEvents.refreshEvent)

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

      val subProjects = subProjectRepository.getAllByProjectId(projectId)
      val nodes = subProjects.map { sp ->
        val spId = sp.id!!
        val folders = folderRepository.getAllBySubProjectId(spId)
        val folderNodes = folders.map { folder ->
          val requests = requestRepository.getAllByFolderId(folder.id!!)
          FolderNode(folder = folder, requests = requests)
        }
        val looseRequests = requestRepository.getAllLooseBySubProjectId(spId)

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
