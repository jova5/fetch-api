package ba.fluxor.fetchapi.feature.tabs.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.LocalWindowHeight
import ba.fluxor.fetchapi.component.*
import ba.fluxor.fetchapi.feature.request.data.RequestHeaderDerivation
import ba.fluxor.fetchapi.feature.settings.viewmodel.SettingsViewModel
import ba.fluxor.fetchapi.feature.tabs.ui.request.*
import ba.fluxor.fetchapi.feature.tabs.viewmodel.RequestExecution
import ba.fluxor.fetchapi.feature.tabs.viewmodel.RequestTab
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabBuffer
import ba.fluxor.fetchapi.network.http.HttpMethod
import ba.fluxor.fetchapi.ui.theme.ThemeMode
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Cursor

@Composable
fun RequestTabEditor(
  buffer: TabBuffer.Request,
  isDirty: Boolean,
  execution: RequestExecution,
  onChange: (TabBuffer) -> Unit,
  onSave: () -> Unit,
  onSend: () -> Unit,
) {
  var selectedTab by remember { mutableStateOf(RequestTab.entries.first()) }
  var autoHidden by remember { mutableStateOf(false) }
  val settingsVm: SettingsViewModel = koinViewModel()
  val state by settingsVm.state.collectAsStateWithLifecycle()
  val isDark = when (state.themeMode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
  }

  Column(modifier = Modifier.fillMaxSize()) {

    Column(modifier = Modifier
      .weight(1f)
      .fillMaxWidth()
      .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {

      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        CompactInput(
          value = buffer.name,
          onValueChange = { onChange(buffer.copy(name = it)) },
        )
        Spacer(Modifier.weight(1f))
        SquareButton(
          text = stringResource(Res.string.save),
          onClick = onSave,
          enabled = isDirty,
          modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp),
          containerColor = MaterialTheme.colorScheme.tertiary,
          contentColor = MaterialTheme.colorScheme.onTertiary
        )
      }
      Spacer(Modifier.height(12.dp))
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        SimpleDropdown(
          options = HttpMethod.entries.map { it.name },
          selected = buffer.method,
          onSelect = { onChange(buffer.copy(method = it)) },
          width = 100.dp,
          optionLabel = { it },
        )
        Spacer(Modifier.width(8.dp))
        CompactInput(
          value = buffer.url,
          onValueChange = { newUrl ->
            val newParams = UrlParamSync.mergeFromUrl(newUrl, buffer.params)
            onChange(buffer.copy(url = newUrl, params = newParams))
          },
          placeholder = stringResource(Res.string.url),
          modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        SquareButton(
          text = stringResource(Res.string.send),
          onClick = onSend,
          enabled = execution !is RequestExecution.Loading,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
      }

      Spacer(Modifier.height(12.dp))
      Divider(thickness = 1.dp)
      Spacer(Modifier.height(12.dp))

      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        RequestTab.entries.forEach { tab ->
          SquareOutlineButton(
            text = stringResource(tab.labelRes()),
            onClick = { selectedTab = tab },
            modifier = Modifier.padding(horizontal = 16.dp),
            borderWidth = if (selectedTab == tab) 2.dp else 0.dp,
          )
        }
      }
      Spacer(Modifier.height(8.dp))
      Divider(thickness = 1.dp)
      Spacer(Modifier.height(16.dp))

      Box(modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
      ) {
        when (selectedTab) {
          RequestTab.PARAMS -> ParamsSection(
            params = buffer.params,
            onChange = { newParams ->
              val newUrl = UrlParamSync.rebuildUrl(buffer.url, newParams)
              onChange(buffer.copy(params = newParams, url = newUrl))
            },
          )

          RequestTab.AUTHORIZATION -> RequestAuthorizationSection(
            authType = buffer.authType,
            authConfig = buffer.authConfig,
            onChange = { newType, newConfig ->
              onChange(buffer.copy(authType = newType, authConfig = newConfig))
            },
          )

          RequestTab.HEADERS -> {
            val resolvedAuth = RequestHeaderDerivation.resolvedAuth(
              buffer.authType,
              buffer.authConfig,
              buffer.parentAuthType,
              buffer.parentAuthConfig,
            )
            HeadersSection(
              buffer = buffer,
              resolvedAuthHeaders = RequestHeaderDerivation.authHeaders(resolvedAuth),
              autoHidden = autoHidden,
              onCustomHeadersChange = { newHeaders -> onChange(buffer.copy(headers = newHeaders)) },
              onReadOnlyToggle = { key, included ->
                val updated = if (included)
                  buffer.excludedAutoHeaders - key
                else
                  buffer.excludedAutoHeaders + key
                onChange(buffer.copy(excludedAutoHeaders = updated))
              },
              onToggleAutoHidden = { autoHidden = !autoHidden },
            )
          }

          RequestTab.BODY -> BodySection(
            body = buffer.body,
            drafts = buffer.bodyDrafts,
            onChange = { newBody, newDrafts ->
              onChange(buffer.copy(body = newBody, bodyDrafts = newDrafts))
            },
          )
        }
      }
    }

    val settingState by settingsVm.state.collectAsStateWithLifecycle()
    val density = LocalDensity.current

    val windowHeight = LocalWindowHeight.current

    val minHeight = 225.dp
    val maxHeight = windowHeight * 0.60f

    var percentage by remember { mutableStateOf(settingState.requestDividerPercentage) }

    val height by remember(windowHeight, percentage) {
      derivedStateOf {
        (minHeight + (maxHeight - minHeight) * percentage)
          .coerceIn(minHeight, maxHeight)
      }
    }
    var virtualMouseY by remember { mutableStateOf(height) }

    val dragState = rememberDraggableState { delta ->
      val deltaDp = with(density) { delta.toDp() }

      virtualMouseY -= deltaDp

      if (virtualMouseY in minHeight..maxHeight) {
        val range = maxHeight - minHeight
        if (range > 0.dp) {
          percentage = ((virtualMouseY - minHeight) / range).coerceIn(0f, 1f)
        }
      }
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isDragging by interactionSource.collectIsDraggedAsState()
    val borderColor = if (isHovered || isDragging) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outlineVariant

    Column(modifier = Modifier.fillMaxWidth()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(2.dp)
          .wrapContentHeight(unbounded = true),
        contentAlignment = Alignment.Center,
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR)))
            .draggable(
              state = dragState,
              orientation = Orientation.Vertical,
              onDragStarted = {
                virtualMouseY = height
              },
              onDragStopped = {
                settingsVm.setRequestDividerPercentage(percentage)
              }
            ),
          contentAlignment = Alignment.Center,
        ) {
          if (isHovered || isDragging) {
            HorizontalDivider(thickness = 2.dp, color = borderColor)
          } else {
            HorizontalDivider(thickness = 2.dp, color = borderColor)
          }
        }
      }

      ResponseView(execution, isDark, height)
    }
  }
}

private enum class ResponseTab {
  BODY,
  HEADERS
}

@Composable
fun ResponseView(
  execution: RequestExecution,
  isDark: Boolean = false,
  height: Dp
) {

  Box(
    modifier = Modifier
      .height(height)
      .fillMaxWidth()
  ) {
    when (execution) {
      RequestExecution.Idle -> ResponseCenteredMessage(
        text = stringResource(Res.string.response_placeholder),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      RequestExecution.Loading -> Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
          Spacer(Modifier.height(8.dp))
          Text(
            text = stringResource(Res.string.response_sending),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      is RequestExecution.Failure -> ResponseCenteredMessage(
        text = execution.message,
        color = MaterialTheme.colorScheme.error,
      )

      is RequestExecution.Success -> ResponseSuccess(execution, isDark)
    }
  }
}

@Composable
private fun ResponseCenteredMessage(text: String, color: Color) {
  Box(
    modifier = Modifier.fillMaxSize()
      .padding(16.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = color)
  }
}

@Composable
private fun ResponseSuccess(success: RequestExecution.Success, isDark: Boolean) {
  val response = success.response
  var selectedTab by remember { mutableStateOf(ResponseTab.BODY) }

  Column(modifier = Modifier.fillMaxSize()) {
    // Meta row: status, time, size
    Row(
      modifier = Modifier.fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "${stringResource(Res.string.response_status)}: ${response.statusCode}",
        style = MaterialTheme.typography.bodySmall,
        color = statusColor(response.statusCode),
      )
      Spacer(Modifier.width(16.dp))
      Text(
        text = "${stringResource(Res.string.response_time)}: ${success.durationMs} ms",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(Modifier.width(16.dp))
      Text(
        text = "${stringResource(Res.string.response_size)}: ${formatSize(response.body)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(Modifier.weight(1f))
      SquareOutlineButton(
        text = stringResource(Res.string.body),
        onClick = { selectedTab = ResponseTab.BODY },
        modifier = Modifier.padding(horizontal = 4.dp),
        borderWidth = if (selectedTab == ResponseTab.BODY) 2.dp else 0.dp,
      )
      SquareOutlineButton(
        text = stringResource(Res.string.headers),
        onClick = { selectedTab = ResponseTab.HEADERS },
        modifier = Modifier.padding(horizontal = 4.dp),
        borderWidth = if (selectedTab == ResponseTab.HEADERS) 2.dp else 0.dp,
      )
    }
    HorizontalDivider(thickness = 1.dp)

    when (selectedTab) {
      ResponseTab.BODY -> {
        val content = remember(response, isDark) { formatAndHighlightJson(response.body, isDark) }
        ResponseScrollableText(content, modifier = Modifier.weight(1f))
      }

      ResponseTab.HEADERS -> KeyValueReadOnlyTable(
        rows = response.headers.entries.map { (key, values) -> key to values.joinToString(", ") },
        keyLabel = stringResource(Res.string.variables_key),
        valueLabel = stringResource(Res.string.api_key_value),
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun ResponseScrollableText(text: AnnotatedString, modifier: Modifier = Modifier) {
  val verticalScrollState = rememberScrollState()
  val horizontalScrollState = rememberScrollState()

  SelectionContainer(modifier = modifier) {
    Box(modifier = Modifier.fillMaxSize()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .autoScrollOnEdgeDrag(verticalScrollState)
          .verticalScroll(verticalScrollState)
          .horizontalScroll(horizontalScrollState)
          .padding(12.dp)
          .padding(end = 12.dp, bottom = 12.dp)
      ) {
        Text(
          text = text,
          style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        )
      }

      VerticalScrollbar(
        adapter = rememberScrollbarAdapter(verticalScrollState),
        modifier = Modifier
          .width(4.dp)
          .align(Alignment.CenterEnd)
          .fillMaxHeight()
          .padding(vertical = 4.dp),
        style = LocalScrollbarStyle.current.copy(
          unhoverColor = MaterialTheme.colorScheme.outlineVariant,
          hoverColor = MaterialTheme.colorScheme.primary,
        ),
      )

      HorizontalScrollbar(
        adapter = rememberScrollbarAdapter(horizontalScrollState),
        modifier = Modifier
          .height(4.dp)
          .align(Alignment.BottomStart)
          .fillMaxWidth()
          .padding(horizontal = 4.dp),
        style = LocalScrollbarStyle.current.copy(
          unhoverColor = MaterialTheme.colorScheme.outlineVariant,
          hoverColor = MaterialTheme.colorScheme.primary,
        ),
      )
    }
  }
}

@Composable
private fun statusColor(code: Int): Color = when (code) {
  in 200..299 -> Color(0xFF4CAF50)
  in 300..399 -> Color(0xFF00BCD4)
  in 400..499 -> Color(0xFFFF9800)
  in 500..599 -> Color(0xFFF44336)
  else -> MaterialTheme.colorScheme.onSurface
}

private fun formatSize(body: String): String {
  val bytes = body.toByteArray(Charsets.UTF_8).size
  return if (bytes < 1024) "$bytes B" else "%.1f KB".format(bytes / 1024.0)
}
