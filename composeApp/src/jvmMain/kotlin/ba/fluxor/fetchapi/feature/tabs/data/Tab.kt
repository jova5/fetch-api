package ba.fluxor.fetchapi.feature.tabs.data

enum class TabType { SUB_PROJECT, FOLDER, REQUEST }

data class Tab(
  val id: Long? = null,
  val projectId: Long,
  val type: TabType,
  val entityId: Long,
  val position: Int,
)
