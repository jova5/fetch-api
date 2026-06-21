package ba.fluxor.fetchapi.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * Auto-scrolls [scrollState] while the pointer is pressed and dragged into the top or bottom
 * edge band of this element. Pointer events are only observed (in the Initial pass) and never
 * consumed, so an underlying SelectionContainer / BasicTextField keeps extending its selection
 * over the text that scrolls into view — the standard "drag a selection past the edge" behaviour.
 *
 * @param edgeSize height of the top/bottom band that triggers scrolling.
 * @param maxSpeedPerFrame scroll distance applied each frame at the very edge (scales down to
 *   zero towards the inner border of the band).
 */
fun Modifier.autoScrollOnEdgeDrag(
  scrollState: ScrollState,
  edgeSize: Dp = 48.dp,
  maxSpeedPerFrame: Dp = 12.dp,
): Modifier = pointerInput(scrollState) {
  val edgePx = edgeSize.toPx()
  val maxSpeedPx = maxSpeedPerFrame.toPx()

  // velocity: negative scrolls up, positive scrolls down; 0 means "not at an edge".
  var velocity = 0f

  coroutineScope {
    var scrollJob: Job? = null

    awaitPointerEventScope {
      while (true) {
        val event = awaitPointerEvent(PointerEventPass.Initial)
        val pointer = event.changes.firstOrNull()

        velocity = if (pointer == null || !event.changes.any { it.pressed }) {
          0f
        } else {
          val y = pointer.position.y
          val height = size.height.toFloat()
          when {
            y < edgePx -> -((edgePx - y) / edgePx).coerceIn(0f, 1f) * maxSpeedPx
            y > height - edgePx -> ((y - (height - edgePx)) / edgePx).coerceIn(0f, 1f) * maxSpeedPx
            else -> 0f
          }
        }

        if (velocity == 0f) {
          scrollJob?.cancel()
          scrollJob = null
        } else if (scrollJob?.isActive != true) {
          scrollJob = launch {
            while (isActive && velocity != 0f) {
              scrollState.scrollBy(velocity)
              delay(16.milliseconds)
            }
          }
        }
      }
    }
  }
}
