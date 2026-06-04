package ba.fluxor.fetchapi.feature.tabs.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.component.TooltipBelow
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabItem
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.close_tab
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/** Distance (px) the tab list scrolls per vertical mouse-wheel notch. Tunable. */
private const val WHEEL_FACTOR = 64f

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TabBar(
  tabs: List<TabItem>,
  selectedTabId: Long?,
  onSelect: (Long) -> Unit,
  onClose: (Long) -> Unit,
) {
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()
  var isBarHovered by remember { mutableStateOf(false) }

  // Bring the selected tab into view when selection changes (e.g. opened from the project tree).
  // No-op when the tab is already fully visible, so clicking a visible tab doesn't jump the bar.
  LaunchedEffect(selectedTabId, tabs.size) {
    val index = tabs.indexOfFirst { it.id == selectedTabId }
    if (index < 0) return@LaunchedEffect
    val info = listState.layoutInfo
    val visible = info.visibleItemsInfo.firstOrNull { it.index == index }
    val clipped = visible == null ||
      visible.offset < info.viewportStartOffset ||
      visible.offset + visible.size > info.viewportEndOffset
    if (clipped) listState.animateScrollToItem(index)
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(36.dp)
      .onPointerEvent(PointerEventType.Enter) { isBarHovered = true }
      .onPointerEvent(PointerEventType.Exit) { isBarHovered = false },
  ) {
    LazyRow(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        // Translate plain vertical wheel into horizontal scroll (no Shift needed).
        .onPointerEvent(PointerEventType.Scroll) { event ->
          val dy = event.changes.first().scrollDelta.y
          if (dy != 0f) scope.launch { listState.scrollBy(dy * WHEEL_FACTOR) }
        },
    ) {
      itemsIndexed(tabs) { index, tab ->
        TabChip(
          tab = tab,
          selected = tab.id == selectedTabId,
          onSelect = { onSelect(tab.id) },
          onClose = { onClose(tab.id) },
          isFirst = index == 0,
        )
      }
    }

    if (isBarHovered) {
      HorizontalScrollbar(
        adapter = rememberScrollbarAdapter(listState),
        modifier = Modifier
          .height(2.dp)
          .align(Alignment.BottomStart)
          .fillMaxWidth(),
        style = LocalScrollbarStyle.current.copy(
          unhoverColor = MaterialTheme.colorScheme.outlineVariant,
          hoverColor = MaterialTheme.colorScheme.primary,
        ),
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabChip(
  tab: TabItem,
  selected: Boolean,
  onSelect: () -> Unit,
  onClose: () -> Unit,
  isFirst: Boolean = false,
) {
  val bg =
    if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()
  val borderColor = MaterialTheme.colorScheme.primary
  var isOverflowing by remember { mutableStateOf(false) }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxHeight()
      .width(150.dp)
      .background(bg)
      .drawBehind {
        val strokeWidth = 1.dp.toPx()

        // Left Border
        if (isFirst) {
          drawLine(
            color = borderColor,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = strokeWidth
          )
        }

        // Right Border
        drawLine(
          color = borderColor,
          start = Offset(size.width, 0f),
          end = Offset(size.width, size.height),
          strokeWidth = strokeWidth - 1
        )
      }
      .hoverable(interactionSource)
      .clickable(onClick = onSelect)
      .onClick(
        matcher = PointerMatcher.mouse(PointerButton(2)),
        onClick = onClose
      )
      .padding(start = 4.dp, end = 4.dp),
  ) {
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight()
        .wrapContentHeight(Alignment.CenterVertically),
    ) {

      Row {
        if (tab.isDirty) {
          Text(
            text = "• ",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
          )
        }
        TooltipBelow(
          text = tab.title.ifBlank { "Untitled" },
        ) {
          Text(
            onTextLayout = { textLayoutResult ->
              isOverflowing = textLayoutResult.hasVisualOverflow
            },
            modifier = Modifier.fillMaxWidth()
              .padding(end = if (isHovered && isOverflowing) 16.dp else 0.dp),
            text = tab.title.ifBlank { "Untitled" },
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
          )
        }
      }
      if (isHovered) {
        IconButton(
          onClick = onClose,
          modifier = Modifier.size(16.dp)
            .align(Alignment.CenterEnd)
        ) {
          Icon(
            Icons.Default.Close,
            contentDescription = stringResource(Res.string.close_tab),
            modifier = Modifier.size(14.dp),
          )
        }
      }
    }
  }
}
