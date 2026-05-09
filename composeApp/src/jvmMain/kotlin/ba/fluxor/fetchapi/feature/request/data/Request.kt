package ba.fluxor.fetchapi.feature.request.data

data class Request(
  val id: Long? = null,
  val subProjectId: Long,
  val folderId: Long? = null,
  val name: String,
  val method: String,
  val url: String,
  val params: List<KeyValueEntry> = emptyList(),
  val headers: List<KeyValueEntry> = emptyList(),
  val body: BodyConfig = BodyConfig.None,
  val authType: String = "INHERIT",
  val authConfig: String? = null,
)
