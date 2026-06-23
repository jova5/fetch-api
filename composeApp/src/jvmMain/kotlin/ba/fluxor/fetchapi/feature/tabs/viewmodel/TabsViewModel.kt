package ba.fluxor.fetchapi.feature.tabs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.fluxor.fetchapi.feature.folder.data.Folder
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderEvent
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderEvents
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderViewModel
import ba.fluxor.fetchapi.feature.request.data.BodyConfig
import ba.fluxor.fetchapi.feature.request.data.Request
import ba.fluxor.fetchapi.feature.request.data.RequestNetworkMapper
import ba.fluxor.fetchapi.feature.request.viewmodel.RequestEvent
import ba.fluxor.fetchapi.feature.request.viewmodel.RequestEvents
import ba.fluxor.fetchapi.feature.request.viewmodel.RequestViewModel
import ba.fluxor.fetchapi.feature.sub_project.data.SubProject
import ba.fluxor.fetchapi.feature.sub_project.data.SubProjectVariable
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectEvent
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectEvents
import ba.fluxor.fetchapi.feature.settings.viewmodel.SettingsViewModel
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectViewModel
import ba.fluxor.fetchapi.feature.tabs.data.TabRepository
import ba.fluxor.fetchapi.feature.tabs.data.TabType
import ba.fluxor.fetchapi.network.http.HttpEngine
import ba.fluxor.fetchapi.network.http.HttpResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

class TabsViewModel(
  private val tabRepository: TabRepository,
  private val subProjectViewModel: SubProjectViewModel,
  private val folderViewModel: FolderViewModel,
  private val requestViewModel: RequestViewModel,
  private val settingsViewModel: SettingsViewModel,
) : ViewModel() {

  private val _state = MutableStateFlow(TabsUiState())
  val state: StateFlow<TabsUiState> = _state.asStateFlow()

  private val _focusReveals = MutableSharedFlow<FocusTarget>(extraBufferCapacity = 1)
  val focusReveals: SharedFlow<FocusTarget> = _focusReveals.asSharedFlow()

  init {
    viewModelScope.launch {
      merge(
        SubProjectEvents.events
          .filterIsInstance<SubProjectEvent.Refresh>()
          .map { },
        FolderEvents.events
          .filterIsInstance<FolderEvent.Refresh>()
          .map { },
        RequestEvents.events
          .filterIsInstance<RequestEvent.Refresh>()
          .map { }
      ).collect {
        syncWithEntities()
      }
    }

    viewModelScope.launch {
      merge(
        SubProjectEvents.events,
        FolderEvents.events,
        RequestEvents.events
      ).collect { event ->
        when (event) {
          is SubProjectEvent.Created -> {
            val newSubProject = event.subProject
            openSubProjectTab(newSubProject)
          }

          is FolderEvent.Created -> {
            val newFolder = event.folder
            openFolderTab(newFolder)
          }

          is RequestEvent.Created -> {
            val newRequest = event.request
            openRequestTab(newRequest)
          }

          else -> {}
        }
      }
    }
  }

  fun loadTabsForProject(projectId: Long) {
    viewModelScope.launch {
      val stored = tabRepository.getAllByProjectId(projectId)
      val items = mutableListOf<TabItem>()
      for (tab in stored) {
        val id = tab.id ?: continue
        val item = buildItem(id, tab.type, tab.entityId)
        if (item != null) {
          items += item
        } else {
          tabRepository.delete(id)
        }
      }
      val storedTabId = settingsViewModel.getLastFocusedTabId(projectId)
      val selected = items.find { it.id == storedTabId } ?: items.lastOrNull()
      _state.update {
        TabsUiState(
          projectId = projectId,
          tabs = items,
          selectedTabId = selected?.id,
        )
      }
      selected?.let { _focusReveals.emit(FocusTarget(it.type, it.entityId)) }
    }
  }

  fun clear() {
    _state.update { TabsUiState() }
  }

  fun openSubProjectTab(sp: SubProject) {
    val id = sp.id ?: return
    openTab(TabType.SUB_PROJECT, id) { tabId ->
      val variables = subProjectViewModel.getAllVariablesBySubProjectId(id)
        .map {
          TabBuffer.VariableEntry(it.key, it.value.orEmpty())
        }
      val buffer = TabBuffer.SubProject(sp.name, sp.authType, sp.authConfig, variables)
      TabItem(tabId, TabType.SUB_PROJECT, id, sp.name, buffer, buffer)
    }
  }

  fun openFolderTab(folder: Folder) {
    val id = folder.id ?: return
    openTab(TabType.FOLDER, id) { tabId ->
      val buffer = TabBuffer.Folder(folder.name)
      TabItem(tabId, TabType.FOLDER, id, folder.name, buffer, buffer)
    }
  }

  fun openRequestTab(request: Request) {
    val id = request.id ?: return
    openTab(TabType.REQUEST, id) { tabId ->
      val parent = subProjectViewModel.getById(request.subProjectId)
      val buffer = TabBuffer.Request(
        name = request.name,
        method = request.method,
        url = request.url,
        params = request.params,
        headers = request.headers,
        body = request.body,
        bodyDrafts = BodyDrafts.from(request.body),
        authType = request.authType,
        authConfig = request.authConfig,
        excludedAutoHeaders = request.excludedAutoHeaders,
        parentAuthType = parent?.authType,
        parentAuthConfig = parent?.authConfig,
      )
      TabItem(tabId, TabType.REQUEST, id, request.name, buffer, buffer)
    }
  }

  private fun openTab(type: TabType, entityId: Long, buildItem: suspend (Long) -> TabItem) {
    val existing = _state.value.tabs.find { it.type == type && it.entityId == entityId }
    if (existing != null) {
      _state.update { it.copy(selectedTabId = existing.id) }
      persistSelectedTab(existing.id)
      return
    }
    val projectId = _state.value.projectId ?: return
    viewModelScope.launch {
      val persisted = tabRepository.findByEntity(projectId, type, entityId)
        ?: tabRepository.create(projectId, type, entityId)
      val tabId = persisted.id ?: return@launch
      val item = buildItem(tabId)
      _state.update { s ->
        if (s.tabs.any { it.id == tabId }) {
          s.copy(selectedTabId = tabId)
        } else {
          s.copy(tabs = s.tabs + item, selectedTabId = tabId)
        }
      }
      persistSelectedTab(tabId)
    }
  }

  fun selectTab(tabId: Long) {
    _state.update { it.copy(selectedTabId = tabId) }
    persistSelectedTab(tabId)
    val tab = _state.value.tabs.find { it.id == tabId } ?: return
    _focusReveals.tryEmit(FocusTarget(tab.type, tab.entityId))
  }

  private fun persistSelectedTab(tabId: Long?) {
    val projectId = _state.value.projectId ?: return
    settingsViewModel.setLastFocusedTabId(projectId, tabId)
  }

  fun closeTab(tabId: Long) {
    viewModelScope.launch {
      tabRepository.delete(tabId)
      _state.update { s ->
        val idx = s.tabs.indexOfFirst { it.id == tabId }
        val newTabs = s.tabs.filterNot { it.id == tabId }
        val newSelected = when {
          s.selectedTabId != tabId -> s.selectedTabId
          newTabs.isEmpty() -> null
          idx <= newTabs.lastIndex -> newTabs[idx].id
          else -> newTabs.last().id
        }
        s.copy(tabs = newTabs, selectedTabId = newSelected)
      }
      persistSelectedTab(_state.value.selectedTabId)

      tabRepository.updatePositions(_state.value.tabs.map { it.id })
    }
  }

  fun updateBuffer(tabId: Long, newBuffer: TabBuffer) {
    _state.update { s ->
      s.copy(tabs = s.tabs.map { if (it.id == tabId) it.copy(buffer = newBuffer) else it })
    }
  }

  fun saveTab(tabId: Long) {
    val tab = _state.value.tabs.find { it.id == tabId } ?: return
    if (!tab.isDirty) return
    viewModelScope.launch {
      val savedBuffer: TabBuffer = when (val buffer = tab.buffer) {
        is TabBuffer.SubProject -> {
          var updated =
            subProjectViewModel.updateSubProject(tab.entityId, buffer.name, buffer.authType,
              buffer.authConfig)
          val cleanedVariables = buffer.variables.filter { it.key.isNotBlank() }
          subProjectViewModel.replaceAllBySubProjectId(
            tab.entityId,
            cleanedVariables.map {
              SubProjectVariable(subProjectId = tab.entityId, key = it.key, value = it.value)
            },
          )
          SubProjectEvents.triggerRefresh()
          updated = updated ?: SubProject(projectId = -1, name = "", authType = "", authConfig = "")
          TabBuffer.SubProject(updated.name, updated.authType, updated.authConfig, cleanedVariables)
        }

        is TabBuffer.Folder -> {
          var updated = folderViewModel.updateFolder(tab.entityId, buffer.name)
          FolderEvents.triggerRefresh()
          updated = updated ?: Folder(subProjectId = -1, name = "")
          TabBuffer.Folder(updated.name)
        }

        is TabBuffer.Request -> {
          val current = requestViewModel.getById(tab.entityId)
          var updated = requestViewModel.updateRequest(
            tab.entityId,
            Request(
              id = tab.entityId,
              subProjectId = current?.subProjectId ?: -1,
              folderId = current?.folderId,
              name = buffer.name,
              method = buffer.method,
              url = buffer.url,
              params = buffer.params,
              headers = buffer.headers,
              body = buffer.body,
              authType = buffer.authType,
              authConfig = buffer.authConfig,
              excludedAutoHeaders = buffer.excludedAutoHeaders,
            ),
          )
          RequestEvents.triggerRefresh()
          updated = updated ?: Request(subProjectId = -1, name = "", method = "", url = "")
          TabBuffer.Request(
            name = updated.name,
            method = updated.method,
            url = updated.url,
            params = updated.params,
            headers = updated.headers,
            body = updated.body,
            bodyDrafts = buffer.bodyDrafts.stash(updated.body),
            authType = updated.authType,
            authConfig = updated.authConfig,
            excludedAutoHeaders = updated.excludedAutoHeaders,
            parentAuthType = buffer.parentAuthType,
            parentAuthConfig = buffer.parentAuthConfig,
          )
        }
      }
      _state.update { s ->
        s.copy(tabs = s.tabs.map {
          if (it.id == tabId) it.copy(
            buffer = savedBuffer,
            original = savedBuffer,
            title = titleOf(savedBuffer),
          ) else it
        })
      }
    }
  }

  fun sendRequest(tabId: Long) {
    val tab = _state.value.tabs.find { it.id == tabId } ?: return
    val buffer = tab.buffer as? TabBuffer.Request ?: return
    if (buffer.url.isBlank()) {
      setExecution(tabId, RequestExecution.Failure("URL is empty"))
      return
    }
    val missingFile = firstMissingFormDataFile(buffer.body)
    if (missingFile != null) {
      setExecution(tabId, RequestExecution.Failure("File not found: $missingFile"))
      return
    }
    setExecution(tabId, RequestExecution.Loading)
    viewModelScope.launch {
      try {
        val request = Request(
          subProjectId = -1, // unused by the mapper
          name = buffer.name,
          method = buffer.method,
          url = buffer.url,
          params = buffer.params,
          headers = buffer.headers,
          body = buffer.body,
          authType = buffer.authType,
          authConfig = buffer.authConfig,
          excludedAutoHeaders = buffer.excludedAutoHeaders,
        )
        val httpRequest = RequestNetworkMapper.toHttpRequest(
          request,
          parentAuthType = buffer.parentAuthType,
          parentAuthConfig = buffer.parentAuthConfig,
          excludedAutoHeaders = buffer.excludedAutoHeaders,
        )
        var response: HttpResponse? = null
        val durationMs = measureTimeMillis { response = HttpEngine.execute(httpRequest) }
        setExecution(tabId, RequestExecution.Success(response!!, durationMs))
      } catch (t: Throwable) {
        setExecution(tabId, RequestExecution.Failure(t.message ?: "Request failed"))
      }
    }
  }

  /** Returns the first selected form-data file that no longer exists on disk, or null. */
  private fun firstMissingFormDataFile(body: BodyConfig): String? {
    if (body !is BodyConfig.FormData) return null
    return body.fields
      .filter { it.enabled && it.isFile }
      .flatMap { it.filePaths }
      .firstOrNull { !java.io.File(it).exists() }
  }

  fun moveTab(fromIndex: Int, toIndex: Int) {
    _state.update { s ->
      if (fromIndex !in s.tabs.indices || toIndex !in s.tabs.indices) return@update s
      val tabs = s.tabs.toMutableList()
      tabs.add(toIndex, tabs.removeAt(fromIndex))
      s.copy(tabs = tabs)
    }
  }

  fun persistTabOrder() {
    viewModelScope.launch {
      tabRepository.updatePositions(_state.value.tabs.map { it.id })
    }
  }

  private fun setExecution(tabId: Long, execution: RequestExecution) {
    _state.update { s ->
      s.copy(tabs = s.tabs.map { if (it.id == tabId) it.copy(execution = execution) else it })
    }
  }

  private suspend fun syncWithEntities() {
    val current = _state.value.tabs
    if (current.isEmpty()) return
    val refreshed = mutableListOf<TabItem>()
    for (item in current) {
      val latest = buildItem(item.id, item.type, item.entityId)
      if (latest == null) {
        tabRepository.delete(item.id)
        continue
      }
      if (item.isDirty) {
        refreshed += item.copy(original = latest.original, title = latest.title)
      } else {
        refreshed += latest.copy(execution = item.execution)
      }
    }
    _state.update { s ->
      val newSelected = when {
        refreshed.any { it.id == s.selectedTabId } -> s.selectedTabId
        refreshed.isEmpty() -> null
        else -> refreshed.last().id
      }
      s.copy(tabs = refreshed, selectedTabId = newSelected)
    }
  }

  private suspend fun buildItem(tabId: Long, type: TabType, entityId: Long): TabItem? {
    return when (type) {
      TabType.SUB_PROJECT -> {
        val sp = subProjectViewModel.getAllByProjectId(_state.value.projectId ?: return null)
          .find { it.id == entityId } ?: return null
        val variables = subProjectViewModel.getAllVariablesBySubProjectId(entityId)
          .map {
            TabBuffer.VariableEntry(it.key, it.value.orEmpty())
          }
        val buffer = TabBuffer.SubProject(sp.name, sp.authType, sp.authConfig, variables)
        TabItem(tabId, type, entityId, sp.name, buffer, buffer)
      }

      TabType.FOLDER -> {
        val folder = findFolder(entityId) ?: return null
        val buffer = TabBuffer.Folder(folder.name)
        TabItem(tabId, type, entityId, folder.name, buffer, buffer)
      }

      TabType.REQUEST -> {
        val request = requestViewModel.getById(entityId) ?: return null
        val parent = subProjectViewModel.getById(request.subProjectId)
        val buffer = TabBuffer.Request(
          name = request.name,
          method = request.method,
          url = request.url,
          params = request.params,
          headers = request.headers,
          body = request.body,
          bodyDrafts = BodyDrafts.from(request.body),
          authType = request.authType,
          authConfig = request.authConfig,
          excludedAutoHeaders = request.excludedAutoHeaders,
          parentAuthType = parent?.authType,
          parentAuthConfig = parent?.authConfig,
        )
        TabItem(tabId, type, entityId, request.name, buffer, buffer)
      }
    }
  }

  private suspend fun findFolder(folderId: Long): Folder? {
    val projectId = _state.value.projectId ?: return null
    val subProjects = subProjectViewModel.getAllByProjectId(projectId)
    for (sp in subProjects) {
      val spId = sp.id ?: continue
      val match = folderViewModel.getAllBySubProjectId(spId)
        .find { it.id == folderId }
      if (match != null) return match
    }
    return null
  }

  private fun titleOf(buffer: TabBuffer): String = when (buffer) {
    is TabBuffer.SubProject -> buffer.name
    is TabBuffer.Folder -> buffer.name
    is TabBuffer.Request -> buffer.name
  }
}
