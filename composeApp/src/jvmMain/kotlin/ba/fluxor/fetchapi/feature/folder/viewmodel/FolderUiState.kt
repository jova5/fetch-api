package ba.fluxor.fetchapi.feature.folder.viewmodel

import ba.fluxor.fetchapi.feature.folder.data.Folder
import org.jetbrains.compose.resources.StringResource

data class FolderUiState(
  val error: StringResource? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
  val showFolderDialog: Boolean = false,
  val editingFolder: Folder? = null,
  val dialogParentSubProjectId: Long? = null,
)
