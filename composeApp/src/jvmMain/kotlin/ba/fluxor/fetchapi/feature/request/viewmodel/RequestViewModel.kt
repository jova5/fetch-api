package ba.fluxor.fetchapi.feature.request.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.fluxor.fetchapi.feature.request.data.Request
import ba.fluxor.fetchapi.feature.request.data.RequestRepository
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.name_can_not_be_empty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RequestViewModel(
  private val requestRepository: RequestRepository
) : ViewModel() {

  private val _state = MutableStateFlow(RequestUiState())
  val state: StateFlow<RequestUiState> = _state.asStateFlow()

  fun createRequest(subProjectId: Long, folderId: Long?) {
    launchCatching {

      val existing = if (folderId != null) {
        requestRepository.getAllByFolderId(folderId).map { it.name }
      } else {
        requestRepository.getAllLooseBySubProjectId(subProjectId).map { it.name }
      }

      val name = uniqueName("New Request", existing)
      val created = requestRepository.create(subProjectId, folderId, name, "GET", "")

      RequestEvents.triggerRequestCreated(created)
    }
  }

  fun updateRequest(id: Long, folderId: Long?, name: String, method: String, url: String,
    headers: String?, body: String?) {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) {
      _state.update { it.copy(error = Res.string.name_can_not_be_empty) }
      return
    }
    launchCatching {
      requestRepository.update(id, folderId, trimmed, method, url, headers, body)
      _state.update { it.copy(error = null) }
      RequestEvents.triggerRefresh()
    }
  }

  fun deleteRequest(id: Long) {
    launchCatching {
      requestRepository.delete(id)
      _state.update { it.copy(error = null) }
      RequestEvents.triggerRefresh()
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

  suspend fun getAllByFolderId(id: Long): List<Request> {
    return requestRepository.getAllByFolderId(id)
  }

  suspend fun getAllLooseBySubProjectId(subProjectId: Long): List<Request> {
    return requestRepository.getAllLooseBySubProjectId(subProjectId)
  }
}

private fun uniqueName(base: String, existing: Collection<String>): String {
  if (base !in existing) return base
  var i = 2
  while ("$base $i" in existing) i++
  return "$base $i"
}
