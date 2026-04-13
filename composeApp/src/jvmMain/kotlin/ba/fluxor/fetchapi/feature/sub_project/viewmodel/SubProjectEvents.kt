package ba.fluxor.fetchapi.feature.sub_project.viewmodel

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SubProjectEvents {
  private val _refreshEvent = MutableSharedFlow<Unit>(replay = 0)
  val refreshEvent = _refreshEvent.asSharedFlow()

  suspend fun triggerRefresh() {
    _refreshEvent.emit(Unit)
  }
}
