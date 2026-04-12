package ba.fluxor.fetchapi.feature.folder.viewmodel

import ba.fluxor.fetchapi.feature.folder.data.Folder
import ba.fluxor.fetchapi.feature.project_tree.viewmodel.SubProjectNode
import ba.fluxor.fetchapi.feature.request.data.Request
import ba.fluxor.fetchapi.feature.sub_project.data.SubProject

data class FolderUiState(
  val error: String? = null,
  val showFolderDialog: Boolean = false,
  val editingFolder: Folder? = null,

  val projectId: Long? = null,
  val subProjectNodes: List<SubProjectNode> = emptyList(),
  val isLoading: Boolean = false,
  val editingSubProject: SubProject? = null,
  val editingRequest: Request? = null,
  val showSubProjectDialog: Boolean = false,
  val showRequestDialog: Boolean = false,
  val dialogParentSubProjectId: Long? = null,
  val dialogParentFolderId: Long? = null,
)
