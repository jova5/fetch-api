package ba.fluxor.fetchapi.feature.tabs.viewmodel

import ba.fluxor.fetchapi.feature.request.data.BodyConfig
import ba.fluxor.fetchapi.feature.request.data.KeyValueEntry
import ba.fluxor.fetchapi.feature.tabs.data.TabType
import ba.fluxor.fetchapi.network.http.HttpResponse

sealed interface TabBuffer {
  data class SubProject(
    val name: String,
    val authType: String,
    val authConfig: String?,
    val variables: List<VariableEntry> = emptyList(),
  ) : TabBuffer

  data class VariableEntry(
    val key: String,
    val value: String,
  )

  data class Folder(
    val name: String,
  ) : TabBuffer

  data class Request(
    val name: String,
    val method: String,
    val url: String,
    val params: List<KeyValueEntry> = emptyList(),
    val headers: List<KeyValueEntry> = emptyList(),
    val body: BodyConfig = BodyConfig.None,
    val bodyDrafts: BodyDrafts = BodyDrafts(),
    val authType: String = "INHERIT",
    val authConfig: String? = null,
    val excludedAutoHeaders: Set<String> = emptySet(),
    val parentAuthType: String? = null,
    val parentAuthConfig: String? = null,
  ) : TabBuffer
}

data class BodyDrafts(
  val raw: BodyConfig.Raw = BodyConfig.Raw(),
  val formData: BodyConfig.FormData = BodyConfig.FormData(),
  val urlEncoded: BodyConfig.UrlEncoded = BodyConfig.UrlEncoded(),
  val binary: BodyConfig.Binary = BodyConfig.Binary(),
) {
  fun stash(body: BodyConfig): BodyDrafts = when (body) {
    is BodyConfig.Raw -> copy(raw = body)
    is BodyConfig.FormData -> copy(formData = body)
    is BodyConfig.UrlEncoded -> copy(urlEncoded = body)
    is BodyConfig.Binary -> copy(binary = body)
    BodyConfig.None -> this
  }

  companion object {
    fun from(body: BodyConfig): BodyDrafts = BodyDrafts().stash(body)
  }
}

sealed interface RequestExecution {
  data object Idle : RequestExecution
  data object Loading : RequestExecution
  data class Success(val response: HttpResponse, val durationMs: Long) : RequestExecution
  data class Failure(val message: String) : RequestExecution
}

data class TabItem(
  val id: Long,
  val type: TabType,
  val entityId: Long,
  val title: String,
  val buffer: TabBuffer,
  val original: TabBuffer,
  val execution: RequestExecution = RequestExecution.Idle,
) {
  val isDirty: Boolean get() = buffer.withoutDrafts() != original.withoutDrafts()
}

private fun TabBuffer.withoutDrafts(): TabBuffer =
  if (this is TabBuffer.Request) copy(bodyDrafts = BodyDrafts()) else this

data class TabsUiState(
  val projectId: Long? = null,
  val tabs: List<TabItem> = emptyList(),
  val selectedTabId: Long? = null,
  val pendingCloseTabId: Long? = null,
) {
  val selectedTab: TabItem? get() = tabs.find { it.id == selectedTabId }
}

/** Identifies the tree entity a focus change points at, so the tree can reveal/scroll to it. */
data class FocusTarget(
  val type: TabType,
  val entityId: Long,
)
