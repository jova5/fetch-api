package ba.fluxor.fetchapi.feature.sub_project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.fluxor.fetchapi.feature.sub_project.data.SubProject
import ba.fluxor.fetchapi.feature.sub_project.data.SubProjectRepository
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.name_can_not_be_empty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubProjectViewModel(
  private val subProjectRepository: SubProjectRepository,
) : ViewModel() {

  private val _state = MutableStateFlow(SubProjectUiState())
  val state: StateFlow<SubProjectUiState> = _state.asStateFlow()

  fun createSubProject(projectId: Long) {
    launchCatching {

      val existing = subProjectRepository.getAllByProjectId(projectId).map { it.name }
      val name = uniqueName("New Sub Project", existing)

      val created = subProjectRepository.create(projectId, name)

      SubProjectEvents.triggerRefresh()
      SubProjectEvents.triggerSubProjectCreated(created)
    }
  }

  private fun uniqueName(base: String, existing: Collection<String>): String {
    if (base !in existing) return base
    var i = 2
    while ("$base $i" in existing) i++
    return "$base $i"
  }

  fun updateSubProject(id: Long, name: String, authType: String, authConfig: String?) {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) {
      _state.update { it.copy(error = Res.string.name_can_not_be_empty) }
      return
    }
    launchCatching {
      subProjectRepository.update(id, trimmed, authType, authConfig)
      _state.update { it.copy(error = null) }
      SubProjectEvents.triggerRefresh()
    }
  }

  fun deleteSubProject(id: Long) {
    launchCatching {
      subProjectRepository.delete(id)
      _state.update { it.copy(error = null) }
      SubProjectEvents.triggerRefresh()
    }
  }

  private fun launchCatching(block: suspend () -> Unit) {
    viewModelScope.launch {
      try {
        block()
      } catch (t: Throwable) {
        _state.update {
          it.copy(isLoading = false, errorMessage = t.message ?: t::class.simpleName)
        }
      }
    }
  }

  suspend fun getAllByProjectId(projectId: Long): List<SubProject> {
    return subProjectRepository.getAllByProjectId(projectId)
  }
}
