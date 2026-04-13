package ba.fluxor.fetchapi.feature.project_tree.viewmodel

import ba.fluxor.fetchapi.feature.request.data.Request
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectNode

data class ProjectTreeUiState(
  val projectId: Long? = null,
  val subProjectNodes: List<SubProjectNode> = emptyList(),
  val isLoading: Boolean = false,
  val error: String? = null,
  val editingRequest: Request? = null,
  val showRequestDialog: Boolean = false,
  val dialogParentSubProjectId: Long? = null,
  val dialogParentFolderId: Long? = null,
)
