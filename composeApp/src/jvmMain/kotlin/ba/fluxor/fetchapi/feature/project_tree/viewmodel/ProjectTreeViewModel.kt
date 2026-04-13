package ba.fluxor.fetchapi.feature.project_tree.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.fluxor.fetchapi.feature.folder.data.FolderRepository
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderEvents
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderNode
import ba.fluxor.fetchapi.feature.request.data.Request
import ba.fluxor.fetchapi.feature.request.data.RequestRepository
import ba.fluxor.fetchapi.feature.sub_project.data.SubProject
import ba.fluxor.fetchapi.feature.sub_project.data.SubProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectTreeViewModel(
  private val subProjectRepository: SubProjectRepository,
  private val folderRepository: FolderRepository,
  private val requestRepository: RequestRepository,
) : ViewModel() {

  private val _state = MutableStateFlow(ProjectTreeUiState())
  val state: StateFlow<ProjectTreeUiState> = _state.asStateFlow()

  init {
    launchCatching {
      FolderEvents.refreshEvent.collect {
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

  // --- SubProject CRUD ---

  fun showNewSubProjectDialog() {
    _state.update { it.copy(showSubProjectDialog = true, editingSubProject = null, error = null) }
  }

  fun showEditSubProjectDialog(subProject: SubProject) {
    _state.update { it.copy(showSubProjectDialog = true, editingSubProject = subProject, error = null) }
  }

  fun createSubProject(name: String) {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) {
      _state.update { it.copy(error = "Name cannot be empty") }
      return
    }
    val projectId = _state.value.projectId ?: return

    launchCatching {
      val exists = subProjectRepository.existsByNameAndProjectId(trimmed, projectId)
      if (exists) {
        _state.update { it.copy(error = "Name already exists") }
        return@launchCatching
      }
      subProjectRepository.create(projectId, trimmed)
      _state.update { it.copy(showSubProjectDialog = false, editingSubProject = null, error = null) }
      loadTree(projectId)
    }
  }

  fun updateSubProject(id: Long, name: String, authType: String, authConfig: String?) {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) {
      _state.update { it.copy(error = "Name cannot be empty") }
      return
    }
    val projectId = _state.value.projectId ?: return

    launchCatching {
      val exists = subProjectRepository.existsByNameAndProjectIdAndIdNot(trimmed, projectId, id)
      if (exists) {
        _state.update { it.copy(error = "Name already exists") }
        return@launchCatching
      }
      subProjectRepository.update(id, trimmed, authType, authConfig)
      _state.update { it.copy(showSubProjectDialog = false, editingSubProject = null, error = null) }
      loadTree(projectId)
    }
  }

  fun deleteSubProject(id: Long) {
    val projectId = _state.value.projectId ?: return
    launchCatching {
      subProjectRepository.delete(id)
      _state.update { it.copy(error = null) }
      loadTree(projectId)
    }
  }

  // --- Request CRUD ---

  fun showNewRequestDialog(subProjectId: Long, folderId: Long?) {
    _state.update {
      it.copy(
        showRequestDialog = true,
        editingRequest = null,
        dialogParentSubProjectId = subProjectId,
        dialogParentFolderId = folderId,
        error = null,
      )
    }
  }

  fun showEditRequestDialog(request: Request) {
    _state.update { it.copy(showRequestDialog = true, editingRequest = request, error = null) }
  }

  fun createRequest(subProjectId: Long, folderId: Long?, name: String, method: String, url: String) {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) {
      _state.update { it.copy(error = "Name cannot be empty") }
      return
    }

    launchCatching {
      requestRepository.create(subProjectId, folderId, trimmed, method, url)
      _state.update { it.copy(showRequestDialog = false, editingRequest = null, error = null) }
      _state.value.projectId?.let { loadTree(it) }
    }
  }

  fun updateRequest(id: Long, folderId: Long?, name: String, method: String, url: String, headers: String?, body: String?) {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) {
      _state.update { it.copy(error = "Name cannot be empty") }
      return
    }

    launchCatching {
      requestRepository.update(id, folderId, trimmed, method, url, headers, body)
      _state.update { it.copy(showRequestDialog = false, editingRequest = null, error = null) }
      _state.value.projectId?.let { loadTree(it) }
    }
  }

  fun deleteRequest(id: Long) {
    launchCatching {
      requestRepository.delete(id)
      _state.update { it.copy(error = null) }
      _state.value.projectId?.let { loadTree(it) }
    }
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

  fun dismissDialogs() {
    _state.update {
      it.copy(
        showSubProjectDialog = false,
        showFolderDialog = false,
        showRequestDialog = false,
        editingSubProject = null,
        editingFolder = null,
        editingRequest = null,
        error = null,
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
