package ba.fluxor.fetchapi.feature.folder.viewmodel

import ba.fluxor.fetchapi.feature.folder.data.Folder
import ba.fluxor.fetchapi.feature.request.data.Request

data class FolderNode(
  val folder: Folder,
  val requests: List<Request> = emptyList(),
  val subFolders: List<FolderNode> = emptyList(),
  val expanded: Boolean = true,
)
