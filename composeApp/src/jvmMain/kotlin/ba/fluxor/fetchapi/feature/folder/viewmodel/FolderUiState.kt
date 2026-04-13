package ba.fluxor.fetchapi.feature.folder.viewmodel

import ba.fluxor.fetchapi.feature.folder.data.Folder

data class FolderUiState(
  val error: String? = null,
  val isLoading: Boolean = false,
  val showFolderDialog: Boolean = false,
  val editingFolder: Folder? = null,
  val dialogParentSubProjectId: Long? = null,
)
