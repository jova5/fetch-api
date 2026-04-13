package ba.fluxor.fetchapi.feature.folder.viewmodel

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object FolderEvents {
  private val _refreshEvent = MutableSharedFlow<Unit>(replay = 0)
  val refreshEvent = _refreshEvent.asSharedFlow()

  suspend fun triggerRefresh() {
    _refreshEvent.emit(Unit)
  }
}
