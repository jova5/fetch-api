package ba.fluxor.fetchapi.feature.sub_project.data

data class SubProjectVariable(
  val id: Long? = null,
  val subProjectId: Long,
  val key: String,
  val value: String? = null,
)
