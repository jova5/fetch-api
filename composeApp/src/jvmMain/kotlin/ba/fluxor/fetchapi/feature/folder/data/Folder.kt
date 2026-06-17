package ba.fluxor.fetchapi.feature.folder.data

data class Folder(
  val id: Long? = null,
  val subProjectId: Long,
  val name: String,
  val parentFolderId: Long? = null,
)
