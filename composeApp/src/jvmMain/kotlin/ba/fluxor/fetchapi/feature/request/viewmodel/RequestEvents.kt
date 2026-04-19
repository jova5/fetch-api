package ba.fluxor.fetchapi.feature.request.viewmodel

import ba.fluxor.fetchapi.feature.request.data.Request
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface RequestEvent {
  object Refresh : RequestEvent
  data class Created(val request: Request) : RequestEvent
}

object RequestEvents {

  private val _events = MutableSharedFlow<RequestEvent>(
    extraBufferCapacity = 16
  )

  val events = _events.asSharedFlow()

  fun triggerRefresh() {
    _events.tryEmit(RequestEvent.Refresh)
  }

  fun triggerRequestCreated(request: Request) {
    _events.tryEmit(RequestEvent.Created(request))
  }
}
