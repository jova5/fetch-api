package ba.fluxor.fetchapi.feature.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.fluxor.fetchapi.feature.project.data.Project
import ba.fluxor.fetchapi.feature.project.data.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectViewModel(
  private val repository: ProjectRepository,
) : ViewModel() {

  private val _state = MutableStateFlow(ProjectUiState())
  val state: StateFlow<ProjectUiState> = _state.asStateFlow()

  init {
    loadAll()
  }

  fun loadAll() {
    launchCatching {
      _state.update { it.copy(isLoading = true, error = null) }
      val projects = repository.getAll()
      _state.update { it.copy(projects = projects, isLoading = false) }
    }
  }

  fun selectById(id: Long) {
    launchCatching {
      val project = repository.getById(id)
      _state.update { it.copy(selected = project, error = null) }
    }
  }

  fun clearSelection() {
    _state.update { it.copy(selected = null, error = null) }
  }

  fun create(name: String) {

    val trimmed = name.trim()

    if (trimmed.isEmpty()) {
      _state.update { it.copy(error = "Name cannot be empty") }
      return
    }

    launchCatching {

      val exists = repository.existsByName(name)

      if (exists) {
        _state.update { it.copy(error = "Name already exists") }
        return@launchCatching
      }

      val created = repository.create(trimmed)

      _state.update {
        it.copy(
          projects = it.projects + created,
          selected = created,
          error = null,
        )
      }
    }
  }

  fun update(id: Long, name: String) {

    val trimmed = name.trim()

    if (trimmed.isEmpty()) {
      _state.update { it.copy(error = "Name cannot be empty") }
      return
    }

    launchCatching {

      val exists = repository.existsByNameAndIdNot(name, id)

      if (exists) {
        _state.update { it.copy(error = "Name already exists") }
        return@launchCatching
      }

      val updated = repository.update(id, trimmed)
      _state.update { ui ->
        ui.copy(
          projects = ui.projects.map { if (it.id == id) updated else it },
          selected = updated,
          error = null,
        )
      }
    }
  }

  fun delete(id: Long) {
    launchCatching {

      repository.delete(id)

      _state.update { ui ->
        ui.copy(
          projects = ui.projects.filterNot { it.id == id },
          selected = if (ui.selected?.id == id) null else ui.selected,
          error = null,
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

  fun setActiveProject(project: Project) {
    launchCatching {
      _state.update { it.copy(isLoading = false, active = project, error = null) }
    }
  }
}
