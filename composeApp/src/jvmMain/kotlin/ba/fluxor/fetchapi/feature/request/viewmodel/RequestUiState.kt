package ba.fluxor.fetchapi.feature.request.viewmodel

import ba.fluxor.fetchapi.feature.request.data.Request

data class RequestUiState(
  val error: String? = null,
  val isLoading: Boolean = false,
  val editingRequest: Request? = null,
  val showRequestDialog: Boolean = false,
  val dialogParentSubProjectId: Long? = null,
  val dialogParentFolderId: Long? = null,
)
