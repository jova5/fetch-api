package ba.fluxor.fetchapi.feature.folder.viewmodel

import ba.fluxor.fetchapi.feature.folder.data.Folder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface FolderEvent {
  object Refresh : FolderEvent
  data class Created(val folder: Folder) : FolderEvent
}

object FolderEvents {

  private val _events = MutableSharedFlow<FolderEvent>(
    extraBufferCapacity = 16
  )

  val events = _events.asSharedFlow()

  fun triggerRefresh() {
    _events.tryEmit(FolderEvent.Refresh)
  }

  fun triggerFolderCreated(folder: Folder) {
    _events.tryEmit(FolderEvent.Created(folder))
  }
}
