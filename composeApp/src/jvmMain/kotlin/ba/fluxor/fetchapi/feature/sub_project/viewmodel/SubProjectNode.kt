package ba.fluxor.fetchapi.feature.sub_project.viewmodel

import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderNode
import ba.fluxor.fetchapi.feature.request.data.Request
import ba.fluxor.fetchapi.feature.sub_project.data.SubProject

data class SubProjectNode(
  val subProject: SubProject,
  val folders: List<FolderNode> = emptyList(),
  val looseRequests: List<Request> = emptyList(),
  val expanded: Boolean = true,
)
