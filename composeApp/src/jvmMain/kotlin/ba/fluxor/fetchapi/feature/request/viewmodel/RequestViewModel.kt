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

  fun createRequest(subProjectId: Long, folderId: Long?, name: String, method: String,
    url: String) {

    val trimmed = name.trim()

    if (trimmed.isEmpty()) {
      _state.update { it.copy(error = Res.string.name_can_not_be_empty) }
      return
    }

    launchCatching {
      requestRepository.create(subProjectId, folderId, trimmed, method, url)
      _state.update { it.copy(showRequestDialog = false, editingRequest = null, error = null) }
      RequestEvents.triggerRefresh()
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
      _state.update { it.copy(showRequestDialog = false, editingRequest = null, error = null) }
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

  fun dismissDialogs() {
    _state.update {
      it.copy(
        showRequestDialog = false,
        editingRequest = null,
        error = null,
      )
    }
  }

  suspend fun getAllByFolderId(id: Long): List<Request> {
    return requestRepository.getAllByFolderId(id)
  }

  suspend fun getAllLooseBySubProjectId(subProjectId: Long): List<Request> {
    return requestRepository.getAllLooseBySubProjectId(subProjectId)
  }
}
