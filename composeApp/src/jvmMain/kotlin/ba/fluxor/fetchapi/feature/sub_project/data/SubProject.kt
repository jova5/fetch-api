package ba.fluxor.fetchapi.feature.sub_project.data

data class SubProject(
  val id: Long? = null,
  val projectId: Long,
  val name: String,
  val authType: String = "NONE",
  val authConfig: String? = null,
)
