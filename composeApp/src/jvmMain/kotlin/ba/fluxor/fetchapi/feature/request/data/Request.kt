package ba.fluxor.fetchapi.feature.request.data

data class Request(
  val id: Long? = null,
  val subProjectId: Long,
  val folderId: Long? = null,
  val name: String,
  val method: String,
  val url: String,
  val headers: String? = null,
  val body: String? = null,
)
