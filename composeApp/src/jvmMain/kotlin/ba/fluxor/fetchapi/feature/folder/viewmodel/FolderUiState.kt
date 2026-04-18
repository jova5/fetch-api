package ba.fluxor.fetchapi.feature.folder.viewmodel

import org.jetbrains.compose.resources.StringResource

data class FolderUiState(
  val error: StringResource? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
)
