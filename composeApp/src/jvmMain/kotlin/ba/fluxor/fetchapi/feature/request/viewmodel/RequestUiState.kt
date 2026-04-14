package ba.fluxor.fetchapi.feature.request.viewmodel

import ba.fluxor.fetchapi.feature.request.data.Request
import org.jetbrains.compose.resources.StringResource

data class RequestUiState(
  val error: StringResource? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
  val editingRequest: Request? = null,
  val showRequestDialog: Boolean = false,
  val dialogParentSubProjectId: Long? = null,
  val dialogParentFolderId: Long? = null,
)
