package ba.fluxor.fetchapi.feature.tabs.viewmodel

import ba.fluxor.fetchapi.feature.tabs.data.TabType

sealed interface TabBuffer {
  data class SubProject(
    val name: String,
    val authType: String,
    val authConfig: String?,
  ) : TabBuffer

  data class Folder(
    val name: String,
  ) : TabBuffer

  data class Request(
    val name: String,
    val method: String,
    val url: String,
    val headers: String?,
    val body: String?,
  ) : TabBuffer
}

data class TabItem(
  val id: Long,
  val type: TabType,
  val entityId: Long,
  val title: String,
  val buffer: TabBuffer,
  val original: TabBuffer,
) {
  val isDirty: Boolean get() = buffer != original
}

data class TabsUiState(
  val projectId: Long? = null,
  val tabs: List<TabItem> = emptyList(),
  val selectedTabId: Long? = null,
) {
  val selectedTab: TabItem? get() = tabs.find { it.id == selectedTabId }
}
