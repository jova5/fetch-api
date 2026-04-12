package ba.fluxor.fetchapi.feature.folder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.fluxor.fetchapi.feature.folder.data.Folder
import ba.fluxor.fetchapi.feature.folder.data.FolderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FolderViewModel(
  private val folderRepository: FolderRepository,
) : ViewModel() {

  private val _state = MutableStateFlow(FolderUiState())
  val state: StateFlow<FolderUiState> = _state.asStateFlow()

  fun showNewFolderDialog(subProjectId: Long) {
    _state.update {
      it.copy(
        showFolderDialog = true,
        editingFolder = null,
        dialogParentSubProjectId = subProjectId,
        error = null
      )
    }
  }

  fun showEditFolderDialog(folder: Folder) {
    _state.update { it.copy(showFolderDialog = true, editingFolder = folder, error = null) }
  }

  fun createFolder(subProjectId: Long, name: String) {

    val trimmed = name.trim()

    if (trimmed.isEmpty()) {
      _state.update { it.copy(error = "Name cannot be empty") }
      return
    }

    launchCatching {

      val exists = folderRepository.existsByNameAndSubProjectId(trimmed, subProjectId)

      if (exists) {
        _state.update { it.copy(error = "Name already exists") }
        return@launchCatching
      }

      folderRepository.create(subProjectId, trimmed)

      _state.update { it.copy(showFolderDialog = false, editingFolder = null, error = null) }
    }
  }

  fun updateFolder(id: Long, subProjectId: Long, name: String) {

    val trimmed = name.trim()

    if (trimmed.isEmpty()) {
      _state.update { it.copy(error = "Name cannot be empty") }
      return
    }

    launchCatching {

      val exists = folderRepository.existsByNameAndSubProjectIdAndIdNot(trimmed, subProjectId, id)

      if (exists) {
        _state.update { it.copy(error = "Name already exists") }
        return@launchCatching
      }

      folderRepository.update(id, trimmed)

      _state.update { it.copy(showFolderDialog = false, editingFolder = null, error = null) }
    }
  }

  fun deleteFolder(id: Long) {
    launchCatching {

      folderRepository.delete(id)

      _state.update { it.copy(error = null) }
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
