package ba.fluxor.fetchapi.feature.sub_project.viewmodel

import ba.fluxor.fetchapi.feature.sub_project.data.SubProject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface SubProjectEvent {
  object Refresh : SubProjectEvent
  data class Created(val subProject: SubProject) : SubProjectEvent
}

object SubProjectEvents {

  private val _events = MutableSharedFlow<SubProjectEvent>(
    extraBufferCapacity = 16
  )

  val events = _events.asSharedFlow()

  fun triggerRefresh() {
    _events.tryEmit(SubProjectEvent.Refresh)
  }

  fun triggerSubProjectCreated(subProject: SubProject) {
    _events.tryEmit(SubProjectEvent.Created(subProject))
  }
}
