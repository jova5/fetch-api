package ba.fluxor.fetchapi.ui.shell.viewmodel

import androidx.lifecycle.ViewModel
import ba.fluxor.fetchapi.feature.project.data.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppShellState(
  val activeProjectId: Long? = null,
  val activeProjectName: String? = null,
)

class AppShellViewModel : ViewModel() {

  private val _state = MutableStateFlow(AppShellState())
  val state: StateFlow<AppShellState> = _state.asStateFlow()

  fun setActiveProject(project: Project) {
    val id = project.id ?: return
    _state.update { it.copy(activeProjectId = id, activeProjectName = project.name) }
  }

  fun clearActiveProject() {
    _state.update { AppShellState() }
  }

  fun onProjectRenamed(id: Long, newName: String) {
    _state.update { if (it.activeProjectId == id) it.copy(activeProjectName = newName) else it }
  }

  fun onProjectDeleted(id: Long) {
    _state.update { if (it.activeProjectId == id) AppShellState() else it }
  }
}
