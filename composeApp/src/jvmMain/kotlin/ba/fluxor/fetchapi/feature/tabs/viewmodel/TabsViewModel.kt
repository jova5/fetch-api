package ba.fluxor.fetchapi.feature.tabs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.fluxor.fetchapi.feature.folder.data.Folder
import ba.fluxor.fetchapi.feature.folder.data.FolderRepository
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderEvent
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderEvents
import ba.fluxor.fetchapi.feature.request.data.Request
import ba.fluxor.fetchapi.feature.request.data.RequestRepository
import ba.fluxor.fetchapi.feature.request.viewmodel.RequestEvent
import ba.fluxor.fetchapi.feature.request.viewmodel.RequestEvents
import ba.fluxor.fetchapi.feature.sub_project.data.SubProject
import ba.fluxor.fetchapi.feature.sub_project.data.SubProjectRepository
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectEvent
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectEvents
import ba.fluxor.fetchapi.feature.tabs.data.TabRepository
import ba.fluxor.fetchapi.feature.tabs.data.TabType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TabsViewModel(
  private val tabRepository: TabRepository,
  private val subProjectRepository: SubProjectRepository,
  private val folderRepository: FolderRepository,
  private val requestRepository: RequestRepository,
) : ViewModel() {

  private val _state = MutableStateFlow(TabsUiState())
  val state: StateFlow<TabsUiState> = _state.asStateFlow()

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
      _state.update {
        TabsUiState(
          projectId = projectId,
          tabs = items,
          selectedTabId = items.lastOrNull()?.id,
        )
      }
    }
  }

  fun clear() {
    _state.update { TabsUiState() }
  }

  fun openSubProjectTab(sp: SubProject) {
    val id = sp.id ?: return
    openTab(TabType.SUB_PROJECT, id) { tabId ->
      val buffer = TabBuffer.SubProject(sp.name, sp.authType, sp.authConfig)
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
      val buffer = TabBuffer.Request(
        name = request.name,
        method = request.method,
        url = request.url,
        headers = request.headers,
        body = request.body,
      )
      TabItem(tabId, TabType.REQUEST, id, request.name, buffer, buffer)
    }
  }

  private fun openTab(type: TabType, entityId: Long, buildItem: (Long) -> TabItem) {
    val existing = _state.value.tabs.find { it.type == type && it.entityId == entityId }
    if (existing != null) {
      _state.update { it.copy(selectedTabId = existing.id) }
      return
    }
    val projectId = _state.value.projectId ?: return
    viewModelScope.launch {
      val persisted = tabRepository.findByEntity(projectId, type, entityId)
        ?: tabRepository.create(projectId, type, entityId)
      val tabId = persisted.id ?: return@launch
      _state.update { s ->
        if (s.tabs.any { it.id == tabId }) {
          s.copy(selectedTabId = tabId)
        } else {
          s.copy(tabs = s.tabs + buildItem(tabId), selectedTabId = tabId)
        }
      }
    }
  }

  fun selectTab(tabId: Long) {
    _state.update { it.copy(selectedTabId = tabId) }
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
          val updated = subProjectRepository.update(tab.entityId, buffer.name, buffer.authType, buffer.authConfig)
          SubProjectEvents.triggerRefresh()
          TabBuffer.SubProject(updated.name, updated.authType, updated.authConfig)
        }
        is TabBuffer.Folder -> {
          val updated = folderRepository.update(tab.entityId, buffer.name)
          FolderEvents.triggerRefresh()
          TabBuffer.Folder(updated.name)
        }
        is TabBuffer.Request -> {
          val current = requestRepository.getById(tab.entityId)
          val updated = requestRepository.update(
            tab.entityId,
            current?.folderId,
            buffer.name,
            buffer.method,
            buffer.url,
            buffer.headers,
            buffer.body,
          )
          RequestEvents.triggerRefresh()
          TabBuffer.Request(
            name = updated.name,
            method = updated.method,
            url = updated.url,
            headers = updated.headers,
            body = updated.body,
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
        refreshed += latest
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
        val sp = subProjectRepository.getAllByProjectId(_state.value.projectId ?: return null)
          .find { it.id == entityId } ?: return null
        val buffer = TabBuffer.SubProject(sp.name, sp.authType, sp.authConfig)
        TabItem(tabId, type, entityId, sp.name, buffer, buffer)
      }
      TabType.FOLDER -> {
        val folder = findFolder(entityId) ?: return null
        val buffer = TabBuffer.Folder(folder.name)
        TabItem(tabId, type, entityId, folder.name, buffer, buffer)
      }
      TabType.REQUEST -> {
        val request = requestRepository.getById(entityId) ?: return null
        val buffer = TabBuffer.Request(
          name = request.name,
          method = request.method,
          url = request.url,
          headers = request.headers,
          body = request.body,
        )
        TabItem(tabId, type, entityId, request.name, buffer, buffer)
      }
    }
  }

  private suspend fun findFolder(folderId: Long): Folder? {
    val projectId = _state.value.projectId ?: return null
    val subProjects = subProjectRepository.getAllByProjectId(projectId)
    for (sp in subProjects) {
      val spId = sp.id ?: continue
      val match = folderRepository.getAllBySubProjectId(spId).find { it.id == folderId }
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
